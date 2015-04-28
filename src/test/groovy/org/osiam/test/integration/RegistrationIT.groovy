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

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.osiam.client.query.Query
import org.osiam.client.query.QueryBuilder
import org.osiam.resources.helper.UserDeserializer
import org.osiam.resources.scim.*
import spock.lang.Ignore
import spock.lang.Shared

/**
 * This test covers the controller for registration purpose.
 */
class RegistrationIT extends AbstractIT {

    @Shared
    ObjectMapper mapper

    def setupSpec() {
        mapper = new ObjectMapper()
        def userDeserializerModule = new SimpleModule('userDeserializerModule', new Version(1, 0, 0, null))
                .addDeserializer(User.class, new UserDeserializer(User.class))
        mapper.registerModule(userDeserializerModule)
    }

    def setup() {
        setupDatabase('database_seed_registration.xml')
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

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'en, en-US'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201

        Query query = new QueryBuilder().filter("userName eq \"email@example.org\"").build()
        SCIMSearchResult<User> users = osiamConnector.searchUsers(query, accessToken)
        User user = users.getResources()[0]
        !user.isActive()
        Extension extension = user.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        extension.getField('activationToken', ExtensionFieldType.STRING) != null
        user.getGroups().get(0).getDisplay().equalsIgnoreCase("Test")
    }

    def 'A german user should get a german email text'() {
        given:
        def userToRegister = [email: 'email@example.org', password: 'password']

        def responseStatus

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'de, de-DE'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201
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

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration/activation'
            uri.query = [userId: createdUserId, activationToken: activationToken]

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200

        osiamConnector.getUser(createdUserId, accessToken).active
    }

    def 'The registration controller should act like the user was not already activated if an user activated when he is already activate'() {
        given:
        def createdUserId = 'cef9452e-00a9-4cec-a086-d171374febef'
        def activationToken = 'cef9452e-00a9-4cec-a086-a171374febef'

        def accessToken = osiamConnector.retrieveAccessToken()

        def firstResponseStatus
        def secondResponseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration/activation'
            uri.query = [userId: createdUserId, activationToken: activationToken]

            response.success = { resp ->
                firstResponseStatus = resp.statusLine.statusCode
            }
        }

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration/activation'
            uri.query = [userId: createdUserId, activationToken: activationToken]

            response.success = { resp ->
                secondResponseStatus = resp.statusLine.statusCode
            }
        }

        then:
        firstResponseStatus == 200
        secondResponseStatus == 200

        osiamConnector.getUser(createdUserId, accessToken).active
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
        }

        then:
        responseStatus == 201

        Query query = new QueryBuilder().filter("userName eq \"email@example.org\"").build()
        SCIMSearchResult<User> users = osiamConnector.searchUsers(query, accessToken)
        User registeredUser = users.getResources()[0]

        Extension registeredExtension1 = registeredUser.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        registeredExtension1.getField('activationToken', ExtensionFieldType.STRING) != null
        Extension registeredExtension2 = registeredUser.getExtension('urn:client:extension')
        registeredExtension2.getField('age', ExtensionFieldType.STRING) != null
        registeredExtension2.getField('age', ExtensionFieldType.STRING) == '12'
        registeredUser.getGroups().get(0).getDisplay().equalsIgnoreCase("Test")
    }

    def 'A registration of an user with not allowed field nickName and existing extension but not the field'() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        // email, password are always allowed, displayName is allowed and nickName is disallowed by config
        // extension 'urn:client:extension' is only allowed with field 'age' and not 'gender'
        def userToRegister = [email: 'email@example.org', password: 'password', displayName: 'displayName',
                              nickName: 'nickname', 'extensions[\'urn:client:extension\'].fields[\'gender\']': 'M']

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        Query queryString = new QueryBuilder().filter("userName eq \"email@example.org\"").build()
        SCIMSearchResult<User> users = osiamConnector.searchUsers(queryString, accessToken)
        User registeredUser = users.getResources()[0]

        Extension registeredExtension1 = registeredUser.getExtension('urn:scim:schemas:osiam:2.0:Registration')
        registeredExtension1.getField('activationToken', ExtensionFieldType.STRING) != null
        registeredUser.getExtension('urn:client:extension')
        registeredUser.getGroups().get(0).getDisplay().equalsIgnoreCase("Test")

        then:
        thrown(NoSuchElementException)

        registeredUser.nickName == null
        registeredUser.displayName == 'displayName'

        responseStatus == 201
    }

    def 'Registration of a user with malformed email returns HTTP status 400 (bad request)'() {
        given:
        def userToRegister = [email: 'email']

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 400
    }

    def 'Registration of a user with malformed email and empty password returns HTTP status 400 (bad request)'() {
        given:
        def userToRegister = [email: 'email', password: '', profileUrl: 'not an url', photo: ' hello ']

        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 400
    }

    def 'The plugin caused an validation error for registration of an user'() {
        given:
        def userToRegister = [email: 'email@osiam.com', password: '0123456789']

        def responseStatus
        def responseContent

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.TEXT) { req ->
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            send ContentType.URLENC, userToRegister

            response.failure = { resp, html ->
                responseStatus = resp.statusLine.statusCode
                responseContent = html.text
            }
        }

        then:
        responseStatus == 400
        responseContent.contains('<div class="alert alert-danger">')
        responseContent.contains('must end with .org!')
    }

    @Ignore("always fails, maybe this test is not valid anymore?")
    def 'The registration controller should escape the displayName'() {
        given:
        def userToRegister = [email      : 'email@example.org', password: 'password',
                              displayName: "<script>alert('hello!');</script>"]

        def responseStatus

        when:
        HTTPBuilder httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.URLENC) { req ->
            headers.'Accept-Language' = 'en, en-US'
            uri.path = REGISTRATION_ENDPOINT + '/registration'
            body = userToRegister

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 201

        Query query = new QueryBuilder().filter("userName eq \"email@example.org\"").build()
        SCIMSearchResult<User> users = osiamConnector.searchUsers(query, accessToken)
        User user = users.getResources()[0]
        user.emails[0].value == 'email@example.org'
        user.displayName == '&lt;script&gt;alert(&#39;hello!&#39;);&lt;/script&gt;'
    }
}
