package org.osiam.test.server

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.osiam.resources.helper.UserDeserializer
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT
import spock.lang.Shared

/**
 * This test covers the controller for registration purpose.
 * User: Jochen Todea
 * Date: 05.11.13
 * Time: 11:45
 * Created: with Intellij IDEA
 */
class RegistrationIT extends AbstractIT{

    @Shared def mapper

    def setupSpec() {
        mapper = new ObjectMapper()
        def userDeserializerModule = new SimpleModule("userDeserializerModule", new Version(1, 0, 0, null))
                .addDeserializer(User.class, new UserDeserializer(User.class))
        mapper.registerModule(userDeserializerModule)

        setupDatabase("database_seed_registration.xml")
    }

    def "The registration controller should return an HTML page if a GET request was issued to his '/' path with an access token in the header"() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()

        def responseContent
        def responseContentType
        def responseStatus

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET, ContentType.TEXT) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register"
            headers."Authorization" = "Bearer " + accessToken.getToken()
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
        //HTML should contain only required fields
        responseContent.count("ng-model") == 10
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

        def httpClient2 = new HTTPBuilder(RESOURCE_ENDPOINT)

        httpClient2.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users/" + createdUserId
            headers."Authorization" = "Bearer " + accessToken.getToken()

            response.success = { resp, json ->
                assert resp.statusLine.statusCode == 200
                //verify that an activation token was generated on server side and the user is inactive
                assert json.activation != null
                assert json.active == false
            }
        }
    }

    def getUserAsStringWithExtension() {
        def urn = "extension"
        def extensionData = ["gender":"male","birth":"Wed Oct 30 16:54:00 CET 1985","newsletter":"false"]

        Extension extension = new Extension(urn, extensionData)
        def user = new User.Builder("George")
                .setPassword("password")
                .addExtension(urn, extension)
                .build()

        return mapper.writeValueAsString(user)
    }

    def "The registration controller should activate the user if a GET request was issued to his '/activate' path with an access token in the header and the activation code as parameter"() {
        given:
        def createdUserId = "cef9452e-00a9-4cec-a086-d171374febef"
        def activationCode = "cef9452e-00a9-4cec-a086-a171374febef"

        def accessToken = osiamConnector.retrieveAccessToken()

        def responseStatus
        def activeFlag
        def token

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/register/activate"
            uri.query = [user:createdUserId, token:activationCode]
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
}