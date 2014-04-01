/*
 * Copyright (C) 2013 tarent AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.osiam.test.integration

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import javax.mail.Message

import org.osiam.resources.helper.UserDeserializer
import org.osiam.resources.scim.Email
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.Name
import org.osiam.resources.scim.SCIMSearchResult
import org.osiam.resources.scim.User

import spock.lang.Shared

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest

/**
 * This test covers the controller for registration purpose.
 */
class RegistrationIT extends AbstractIT {

    @Shared
    ObjectMapper mapper

    def mailServer

    def setupSpec() {
        mapper = new ObjectMapper()
        def userDeserializerModule = new SimpleModule('userDeserializerModule', new Version(1, 0, 0, null))
                .addDeserializer(User.class, new UserDeserializer(User.class))
        mapper.registerModule(userDeserializerModule)
    }

    def setup() {
        setupDatabase('database_seed_registration.xml')

        mailServer = new GreenMail(ServerSetupTest.ALL)
        mailServer.start()
    }

    def cleanup() {
        mailServer.stop()
    }

    def 'The registration controller should return a rendered html'() {
        given:
        def responseContent
        def responseContentType
        def responseStatus

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET, ContentType.TEXT) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            headers.Accept = 'text/html'

            response.success = { resp, html ->
                responseStatus = resp.statusLine.statusCode
                responseContentType = resp.headers.'Content-Type'
                responseContent = html.text
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200
        responseContentType.contains(ContentType.HTML.toString())
        //ensure that the content is HTML
        responseContent.contains('</form>')
        //HTML should contain the fields for registration
        responseContent.contains('/registration')
        responseContent.contains('email')
        responseContent.contains('password')
        responseContent.contains('displayName')
        responseContent.contains('urn:client:extension')
    }

    def 'The registration controller should complete the registration process if a POST request send to "/registration"'() {
        given:
        def userToRegister = [email: 'email@example.org', password: 'password']

        def responseStatus
        def createdUserId

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'en, en-US'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201

        def queryString = "filter=" + URLEncoder.encode("userName eq \"email@example.org\"", "UTF-8")
        SCIMSearchResult<User> users = osiamConnector.searchUsers(queryString, accessToken)
        User user = users.getResources()[0];
        !user.isActive()
        Extension extension = user.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        extension.getField('activationToken', ExtensionFieldType.STRING) != null

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages()
        messages.length == 1
        messages[0].getSubject().contains('Confirmation of your registration')
        GreenMailUtil.getBody(messages[0]).contains('your account has been created')
        messages[0].getFrom()[0].toString() == 'noreply@osiam.org'
        messages[0].getAllRecipients()[0].toString().equals('email@example.org')
    }
    
    def 'A german user should get a german email text'() {
        given:
        def userToRegister = [email: 'email@example.org', password: 'password']

        def responseStatus
        def createdUserId

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'de, de-DE'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages()
        messages.length == 1
        messages[0].getSubject().contains('Abschließen der Registrierung')
        GreenMailUtil.getBody(messages[0]).contains('ihr Account wurde erstellt.')
        messages[0].getFrom()[0].toString() == 'noreply@osiam.org'
        messages[0].getAllRecipients()[0].toString().equals('email@example.org')
    }

    def getUserAsStringWithExtension() {
        Name name = new Name.Builder().setFamilyName("Simpson")
                .setFormatted("Homer Simpson").setGivenName("Homer")
                .setHonorificPrefix("Dr.").setHonorificSuffix("Mr.")
                .setMiddleName("J").build()

        Email email = new Email.Builder().setPrimary(true).setValue('email@example.org').build()

        User user = new User.Builder('George Alexander')
                .setPassword('password')
                .setEmails([email])
                .setName(name)
                .build()

        return mapper.writeValueAsString(user)
    }

    def 'The registration controller should activate the user if a POST request was send to "/registration/activation" with user id and activation code as parameter'() {
        given:
        def createdUserId = 'cef9452e-00a9-4cec-a086-d171374febef'
        def activationToken = 'cef9452e-00a9-4cec-a086-a171374febef'

        def accessToken = osiamConnector.retrieveAccessToken()

        def responseStatus
        def activeFlag
        def token

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration/activation'
            uri.query = [userId:createdUserId, activationToken:activationToken]

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200

        osiamConnector.getUser(createdUserId, accessToken).active
        token == null
    }

    def 'A registration of an user with client defined extensions'() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        def userToRegister = [email: 'email@example.org', password: 'password', 'extensions[\'urn:client:extension\'].fields[\'age\']': 12]

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'en, en-US'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201

        def queryString = "filter=" + URLEncoder.encode("userName eq \"email@example.org\"", "UTF-8")
        SCIMSearchResult<User> users = osiamConnector.searchUsers(queryString, accessToken)
        User registeredUser = users.getResources()[0];

        Extension registeredExtension1 = registeredUser.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        registeredExtension1.getField('activationToken', ExtensionFieldType.STRING) != null
        Extension registeredExtension2 = registeredUser.getExtension('urn:client:extension')
        registeredExtension2.getField('age', ExtensionFieldType.STRING) != null
        registeredExtension2.getField('age', ExtensionFieldType.STRING) == '12'

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages()
        messages.length == 1
        messages[0].getSubject().contains('Confirmation of your registration')
        GreenMailUtil.getBody(messages[0]).contains('your account has been created')
        messages[0].getFrom()[0].toString() == 'noreply@osiam.org'
        messages[0].getAllRecipients()[0].toString().equals('email@example.org')
    }

    def 'A registration of an user with not allowed field nickName and existing extension but not the field'() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        // email, password are always allowed, displayName is allowed and nickName is disallowed by config
        // extension 'urn:client:extension' is only allowed with field 'age' and not 'gender'
        def userToRegister = [email: 'email@example.org', password: 'password', displayName: 'displayName', nickName: 'nickname',
            'extensions[\'urn:client:extension\'].fields[\'gender\']': 'M']

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        def queryString = "filter=" + URLEncoder.encode("userName eq \"email@example.org\"", "UTF-8")
        SCIMSearchResult<User> users = osiamConnector.searchUsers(queryString, accessToken)
        User registeredUser = users.getResources()[0];

        Extension registeredExtension1 = registeredUser.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        registeredExtension1.getField('activationToken', ExtensionFieldType.STRING) != null
        registeredUser.getExtension('urn:client:extension')

        then:
        thrown(NoSuchElementException)

        registeredUser.nickName == null
        registeredUser.displayName == 'displayName'

        responseStatus == 201

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages()
        messages.length == 1
    }
    
    def 'A registration of an user with malformed email and blank password gets bad request'() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        def userToRegister = [email: 'email', password: ' ']

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 400
    }
}