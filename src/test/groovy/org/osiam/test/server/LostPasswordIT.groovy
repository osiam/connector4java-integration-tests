package org.osiam.test.server

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT

/**
 * Integration test for lost password controller
 * User: Jochen Todea
 * Date: 15.11.13
 * Time: 13:40
 * Created: with Intellij IDEA
 */
class LostPasswordIT extends AbstractIT {

    def setupSpec() {
        setupDatabase("database_seed_lost_password.xml")
    }

    def "URI: /password/lost/{userId} with POST method for lost password flow activation"() {
        given:
        def urn = "urn:scim:schemas:osiam:1.0:Registration"
        def userId = "cef8452e-00a9-4cec-a086-d171374febef"
        def accessToken = osiamConnector.retrieveAccessToken()
        def statusCode

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/password/lost/" + userId
            headers.'Authorization' = 'Bearer ' + accessToken.getToken()

            response.success = { resp ->
                statusCode = resp.statusLine.statusCode
            }

            response.failure = { resp ->
                statusCode = resp.statusLine.statusCode
            }
        }

        then:
        statusCode == 201
        User user = osiamConnector.getUser(userId, accessToken)
        Extension extension = user.getExtension(urn)
        extension.getField("oneTimePassword") != null
    }

    def "URI: /password/lostForm with GET method to get a form for new password submission"() {
        given:
        def statusCode
        def content
        def otp = "cef9452e-00a9-4cec-a086-a171374febef"
        def userId = "cef9452e-00a9-4cec-a086-d171374febef"

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/password/lostForm"
            uri.query = [otp: otp, userId: userId]
            headers.Accept = 'text/html'

            response.success = { resp, html ->
                statusCode = resp.statusLine.statusCode
                content = html.text
            }

            response.failure = { resp ->
                statusCode = resp.statusLine.statusCode
            }
        }

        then:
        statusCode == 200
        content.contains("</form>")
        content.contains("cef9452e-00a9-4cec-a086-a171374febef")
        content.contains("cef9452e-00a9-4cec-a086-d171374febef")
    }

    def "URI: /password/change with POST method to change the old with the new password and validating the user"() {
        given:
        def urn = "urn:scim:schemas:osiam:1.0:Registration"
        def accessToken = osiamConnector.retrieveAccessToken()
        def otp = "cef9452e-00a9-4cec-a086-a171374febef"
        def userId = "cef9452e-00a9-4cec-a086-d171374febef"
        def newPassword = "pulverToastMann"
        def statusCode
        def savedUserId

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST) { req ->
            uri.path = REGISTRATION_ENDPOINT + "/password/change"
            uri.query = [otp: otp, userId: userId]
            body= newPassword
            headers.'Authorization' = 'Bearer ' + accessToken.getToken()

            response.success = { resp, json ->
                statusCode = resp.statusLine.statusCode
                savedUserId = json.id
            }

            response.failure = { resp ->
                statusCode = resp.statusLine.statusCode
            }
        }

        then:
        statusCode == 200
        savedUserId == userId
        User user = osiamConnector.getUser(userId, accessToken)
        Extension extension = user.getExtension(urn)
        extension.getField("oneTimePassword") == ""
    }
}
