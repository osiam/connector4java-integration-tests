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

import static groovyx.net.http.ContentType.URLENC
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

import javax.mail.Message

import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.User

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetupTest

/**
 * Integration test for lost password controller
 */
class LostPasswordIT extends AbstractIT {

    def mailServer

    def setup() {
        setupDatabase("database_seed_lost_password.xml")
        mailServer = new GreenMail(ServerSetupTest.ALL)
        mailServer.start()
    }

    def cleanup() {
        mailServer.stop()
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
        statusCode == 200
        User user = osiamConnector.getUser(userId, accessToken)
        Extension extension = user.getExtension(urn)
        extension.getField("oneTimePassword", ExtensionFieldType.STRING) != null

        //Waiting at least 5 seconds for an E-Mail but aborts instantly if one E-Mail was received
        mailServer.waitForIncomingEmail(5000, 1)
        Message[] messages = mailServer.getReceivedMessages()
        messages.length == 1
        messages[0].getSubject() == "passwordLost"
        def msg = GreenMailUtil.getBody(messages[0])
        msg.contains('to reset your password, please click the link below:')
        msg.contains(userId)
        messages[0].getFrom()[0].toString() == "noreply@osiam.org"
        messages[0].getAllRecipients()[0].toString().equals("george.alexander@osiam.org")
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

        httpClient.request(Method.POST) {
            uri.path = REGISTRATION_ENDPOINT + "/password/change"
            send URLENC, [oneTimePassword : otp, userId : userId, newPassword : newPassword]
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
        extension.isFieldPresent("oneTimePassword") == false
    }

    def "URI: /password/lostForm with GET method to get an html form with input field for the new password including known values as otp and userId"() {
        given:
        def otp = "otpVal"
        def userId = "userIdVal"

        def statusCode
        def responseContentType
        def responseContent

        when:
        def httpClient = new HTTPBuilder(REGISTRATION_ENDPOINT)

        httpClient.request(Method.GET, ContentType.TEXT) {
            uri.path = REGISTRATION_ENDPOINT + "/password/lostForm"
            uri.query = [oneTimePassword : otp, userId : userId]
            headers.Accept = "text/html"

            response.success = {resp, html ->
                statusCode = resp.statusLine.statusCode
                responseContentType = resp.headers.'Content-Type'
                responseContent = html.text
            }

            response.failure = { resp ->
                statusCode = resp.statusLine.statusCode
            }
        }

        then:
        statusCode == 200
        responseContentType == ContentType.HTML.toString()
        responseContent.contains('\$scope.otp = \'otpVal\'')
        responseContent.contains('\$scope.id = \'userIdVal\'')
        responseContent.count("ng-model") == 2
        responseContent.contains('url: \'http://test\'')
    }
}