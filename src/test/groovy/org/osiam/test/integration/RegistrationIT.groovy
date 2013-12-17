package org.osiam.test.integration

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.osiam.resources.helper.UserDeserializer
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import spock.lang.Shared

import javax.mail.Message

/**
 * This test covers the controller for registration purpose.
 * User: Jochen Todea
 * Date: 05.11.13
 * Time: 11:45
 * Created: with Intellij IDEA
 */
class RegistrationIT extends AbstractIT{

    @Shared def mapper
    @Shared def mailServer

    def setupSpec() {
        mapper = new ObjectMapper()
        def userDeserializerModule = new SimpleModule("userDeserializerModule", new Version(1, 0, 0, null))
                .addDeserializer(User.class, new UserDeserializer(User.class))
        mapper.registerModule(userDeserializerModule)

        mailServer = new GreenMail(ServerSetupTest.ALL)
        mailServer.start()
    }

    def setup() {
        setupDatabase("database_seed_registration.xml")
    }

    def cleanupSpec() {
        mailServer.stop()
    }

    def "The registration controller should return an HTML page if a GET request was issued to its '/' path with an access token in the header"() {
        given:
        def responseContent
        def responseContentType
        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET, ContentType.TEXT) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register"
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
        responseContentType == ContentType.HTML.toString()
        //ensure that the content is HTML
        responseContent.contains("</form>")
        //HTML should contain the fields for registration
        responseContent.count("ng-model") == 8
        responseContent.contains('url: \'http://test\'')
    }

    def "The registration controller should complete the registration process if a POST request was issued to his '/create' path with an access token in the header"() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()
        def userToRegister = getUserAsStringWithExtension()

        def responseStatus
        def createdUserId

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.JSON) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register/create"
            body = userToRegister
            headers."Authorization" = "Bearer " + accessToken.getToken()

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
                createdUserId = json.id
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200

        User user = osiamConnector.getUser(createdUserId, accessToken)
        !user.isActive()
        Extension extension = user.getExtension('urn:scim:schemas:osiam:1.0:Registration')
        extension.getField('activationToken', ExtensionFieldType.STRING) != null

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages();
        messages.length == 1
        messages[0].getSubject() == "registration"
        GreenMailUtil.getBody(messages[0]).trim() == "Sehr geehrter Kunde, für Sie wurde ein Benutzerkonto angelegt. Um Ihre Registrierung zu bestätigen rufen Sie bitte folgenden Link auf: http://test?userId=3D293d0321-0edd-43cf-bbb1-b587c9d8e1e8&activationToken=3Daae165bd-6e00-47e3-9908-e1bd89793e60 Mit freundlichen Grüßen, Ihr OSIAM-Team"
        messages[0].getFrom()[0] == "noreply@osiam.org"
    }

    def getUserAsStringWithExtension() {
        def email = new MultiValuedAttribute(primary: true, value: "email@example.org")

        def user = new User.Builder("George Alexander")
                .setPassword("password")
                .setEmails([email])
                .build()

        return mapper.writeValueAsString(user)
    }

    def "The registration controller should activate the user if a POST request was issued to his '/activate' path with an access token in the header and the activation code as parameter"() {
        given:
        def createdUserId = "cef9452e-00a9-4cec-a086-d171374febef"
        def activationCode = "cef9452e-00a9-4cec-a086-a171374febef"

        def accessToken = osiamConnector.retrieveAccessToken()

        def responseStatus
        def activeFlag
        def token

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register/activate"
            uri.query = [userId:createdUserId, activationToken:activationCode]
            headers."Authorization" = "Bearer " + accessToken.getToken()

            response.success = { resp ->
                responseStatus = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200

        def httpClient3 = new HTTPBuilder(RESOURCE_ENDPOINT)

        httpClient3.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + createdUserId
            headers."Authorization" = "Bearer " + accessToken.getToken()

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
                //verify that the activation token was deleted and user is active
                token = json.'urn:scim:schemas:osiam:1.0:Registration'.activationToken
                activeFlag = json.active
            }
        }

        responseStatus == 200
        activeFlag
        token == ""
    }

    def "Registration of user with client defined extensions"() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        def email = new MultiValuedAttribute(primary: true, value: "email@example.org")
        def extension = new Extension('urn:scim:schemas:osiam:1.0:Test')
        extension.addOrUpdateField("field1", "value1")
        extension.addOrUpdateField("field2", "value2")
        extension.addOrUpdateField("field3", "value3")

        def extensions = [extension] as Set

        def user = new User.Builder("George der II")
                .setPassword("password")
                .setEmails([email])
                .addExtensions(extensions)
                .build()

        def userToRegister = mapper.writeValueAsString(user)

        def responseStatus
        def createdUserId

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST, ContentType.JSON) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register/create"
            body = userToRegister
            headers."Authorization" = "Bearer " + accessToken.getToken()

            response.success = { resp, json ->
                responseStatus = resp.statusLine.statusCode
                createdUserId = json.id
            }

            response.failure = { resp ->
                responseStatus = resp.statusLine.statusCode
            }
        }

        then:
        responseStatus == 200

        User registeredUser = osiamConnector.getUser(createdUserId, accessToken)
        !registeredUser.isActive()
        Extension registeredExtension1 = registeredUser.getExtension('urn:scim:schemas:osiam:1.0:Registration')
        registeredExtension1.getField('activationToken', ExtensionFieldType.STRING) != null
        Extension registeredExtension2 = registeredUser.getExtension('urn:scim:schemas:osiam:1.0:Test')
        registeredExtension2.getField('field1', ExtensionFieldType.STRING) != null

        //TODO check mail
    }
}