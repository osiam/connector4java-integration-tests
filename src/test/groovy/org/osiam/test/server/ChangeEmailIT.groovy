package org.osiam.test.server

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT
import static groovyx.net.http.ContentType.URLENC


/**
 * Integration test for the change email controller.
 * User: Jochen Todea
 * Date: 20.11.13
 * Time: 11:45
 * Created: with Intellij IDEA
 */
class ChangeEmailIT extends AbstractIT {

    def setupSpec() {
        setupDatabase("database_seed_change_email.xml")
    }

    def "The /change endpoint with HTTP method POST should generate confirmation token, saving the new email temporary and sending an email to the new address"() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()
        def userId = "cef8452e-00a9-4cec-a086-d171374febef"
        def newEmailValue = "newEmailForGeorgeAlexander@osiam.org"
        def urn = "urn:scim:schemas:osiam:1.0:Registration"

        def responseStatusCode

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST) {
            uri.path = REGISTRATION_ENDPOINT + "/email/change"
            send URLENC, [userId:userId, newEmailValue: newEmailValue]
            headers.'Authorization' = "Bearer " + accessToken.getToken()

            response.success = { resp ->
                responseStatusCode = resp.statusLine.statusCode

            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }
        }

        then:
        responseStatusCode == 200
        User user = osiamConnector.getUser(userId, accessToken)
        Extension extension = user.getExtension(urn)
        extension.getField("emailConfirmToken", ExtensionFieldType.STRING) != null
        extension.getField("tempEmail", ExtensionFieldType.STRING) == newEmailValue
    }

    def "The /confirm endpoint with HTTP method POST should verify the confirmation token, saving the email as primary email and sending an email to the old address"() {
        given:
        def accessToken = osiamConnector.retrieveAccessToken()
        def userId = "cef9452e-00a9-4cec-a086-d171374febef"
        def confirmToken = "cef9452e-00a9-4cec-a086-a171374febef"
        def urn = "urn:scim:schemas:osiam:1.0:Registration"
        def newEmailValue = "newEmailForGeorge@osiam.org"

        def savedUserId
        def responseStatusCode

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.POST) {
            uri.path = REGISTRATION_ENDPOINT + "/email/confirm"
            send URLENC, [userId:userId, confirmToken: confirmToken]
            headers.'Authorization' = "Bearer " + accessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                savedUserId = json.id
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }
        }

        then:
        responseStatusCode == 200
        userId == savedUserId
        User user = osiamConnector.getUser(userId, accessToken)
        Extension extension = user.getExtension(urn)
        extension.getField("emailConfirmToken", ExtensionFieldType.STRING) == ""
        extension.getField("tempEmail", ExtensionFieldType.STRING) == ""
        user.getEmails()[0].isPrimary()
        user.getEmails()[0].getValue() == newEmailValue
    }
}
