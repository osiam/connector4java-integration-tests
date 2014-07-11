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

import org.osiam.client.exception.UnauthorizedException
import org.osiam.client.oauth.AccessToken
import org.osiam.resources.scim.Email
import org.osiam.resources.scim.User

import spock.lang.Unroll

/**
 * Base class for server integration tests.
 *
 */
class ControllerIT extends AbstractIT {

    def setup() {
        setupDatabase('database_seed.xml')
    }

    @Unroll
    def "REGT-001-#testCase: An API request missing an accept header with scope #scope and content type #contentType on path #requestPath should return HTTP status code #expectedResponseCode and content type #expectedResponseType."() {
        given: "a valid access token"

        when: "a request is sent"
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContentType

        http.request(Method.GET, contentType) { req ->
            uri.path = RESOURCE_ENDPOINT + requestPath
            headers."Authorization" = "Bearer " + accessToken.getToken()

            // response handler for a success response code:
            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContentType = resp.headers."Content-Type"
            }

            // handler for any failure status code:
            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
                contentType = resp.headers."Content-Type"
            }

        }

        then: "the response should be as expected"
        assert responseStatusCode == expectedResponseCode

        expect: "the response type should be as expected"
        assert responseContentType == expectedResponseType

        where:
        testCase | requestPath | contentType        | expectedResponseCode | expectedResponseType
        "a"      | "/Users"    | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "b"      | "/Users/"   | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "c"      | "/Groups"   | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "d"      | "/Groups/"  | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "e"      | "/Users"    | ContentType.ANY    | 200                  | "application/json;charset=UTF-8"
        "f"      | "/Users"    | ContentType.TEXT   | 406                  | null
        "g"      | "/Users"    | ContentType.BINARY | 406                  | null
        "h"      | "/Users"    | ContentType.HTML   | 406                  | null
        "i"      | "/Users"    | ContentType.URLENC | 406                  | null
        "j"      | "/Users"    | ContentType.XML    | 406                  | null
        "k"      | "/Users"    | "invalid"          | 406                  | null
        "l"      | "/Users"    | "/"                | 406                  | null
        "m"      | "/Metrics"  |  ContentType.JSON  | 200                  | "application/json"
        "n"      | "/Metrics/" |  ContentType.JSON  | 200                  | "application/json"
    }

    @Unroll
    def "REGT-002-#testCase: A search operation on the Users endpoint with search string #searchString should return HTTP status code #expectedResponseCode."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a request is sent"
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContentType
        def responseErrorCode

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users"
            uri.query = [filter: searchString]
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            // response handler for a success response code:
            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContentType = resp.headers."Content-Type"
            }

            // handler for any failure status code:
            response.failure = { resp, json ->
                responseErrorCode = json.error_code
                responseStatusCode = resp.statusLine.statusCode
                contentType = resp.headers."Content-Type"
            }

        }

        then: "the response and possible failure codes and text should be as expected"
        assert responseStatusCode == expectedResponseCode
        assert responseErrorCode == expectedResponseErrorCode

        where:
        testCase | searchString                                | expectedResponseCode | expectedResponseErrorCode
        'a'      | 'userName eq "marissa"'                     | 200                  | null                      // String
        'b'      | 'userName co "ari"'                         | 200                  | null                      // String
        'c'      | 'userName sw "mar"'                         | 200                  | null                      // String
        'd'      | 'userName pr'                               | 200                  | null                      // String
        'e'      | 'userName gt "l"'                           | 200                  | null                      // String
        'f'      | 'userName ge "m"'                           | 200                  | null                      // String
        'g'      | 'userName lt "n"'                           | 200                  | null                      // String
        'h'      | 'userName le "m"'                           | 200                  | null                      // String
        'i'      | 'emails.type eq "work"'                     | 200                  | null                      // Enum (EmailEntity)
        'j'      | 'emails.type co "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'k'      | 'emails.type sw "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'l'      | 'emails.type pr'                            | 200                  | null                      // Enum (EmailEntity)
        'm'      | 'emails.type gt "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'n'      | 'emails.type ge "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'o'      | 'emails.type lt "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'p'      | 'emails.type le "work"'                     | 409                  | 'CONFLICT'                 // Enum (EmailEntity)
        'q'      | 'active eq "true"'                          | 200                  | null                      // boolean
        'r'      | 'active co "true"'                          | 409                  | 'CONFLICT'                 // boolean
        's'      | 'active sw "true"'                          | 409                  | 'CONFLICT'                 // boolean
        't'      | 'active pr'                                 | 200                  | null                      // boolean
        'u'      | 'active gt "true"'                          | 409                  | 'CONFLICT'                 // boolean
        'v'      | 'active ge "true"'                          | 409                  | 'CONFLICT'                 // boolean
        'w'      | 'active lt "true"'                          | 409                  | 'CONFLICT'                 // boolean
        'x'      | 'active le "true"'                          | 409                  | 'CONFLICT'                 // boolean
        'y'      | 'meta.created co "2013-08-08T19:46:20.638"' | 409                  | 'CONFLICT'                // Date
        'z'      | 'meta.created sw "2013-08-08T1"'            | 409                  | 'CONFLICT'                // Date
        'za'     | 'active pr "true"'                          | 409                  | 'CONFLICT'                // pr with value
    }

    def "REGT-005: A search filter String matching two users should return totalResults=2 and two unique Resource elements."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a filter request matching two users is sent"
        def http = new HTTPBuilder(RESOURCE_ENDPOINT)

        def responseStatusCode
        def responseContent

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users"
            uri.query = [filter: '(userName eq "cmiller" or userName eq "hsimpson") and meta.created gt "2003-05-23T13:12:45.672"']
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            // response handler for a success response code:
            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }
        }

        then: "the response elements should be unique and as expected"
        assert responseStatusCode == 200
        assert responseContent.totalResults == 2

        assert responseContent.Resources.size() == 2

        // Check uniqueness to prevent counting faulty items. Also check userName's.
        Collection elements = new HashSet()
        responseContent.Resources.each {
            assert elements.add(it) // Returns 'false' if already in HashSet.
            assert (it.toString().contains("cmiller") || it.toString().contains("hsimpson"))
        }
    }

    def 'REGT-OSNG-141: E-Mail address should not be unique. So two different users should be able to add the same address and getting displayed only the own entry.'() {

        given: 'a valid access token and two users with the same E-Mail address'
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def emailUserOne = new Email.Builder().setType(Email.Type.WORK).setValue('sameMail@osiam.de').build()
        def emailUserTwo = new Email.Builder().setType(Email.Type.HOME).setValue('sameMail@osiam.de').build()
        def user1 = new User.Builder('UserOne').addEmails([emailUserOne] as List).setExternalId('pew1').build()
        def user2 = new User.Builder('UserTwo').addEmails([emailUserTwo] as List).setExternalId('pew2').build()

        when: 'a add user request is sent'
        User retUser1 = osiamConnector.createUser(user1, validAccessToken)
        User retUser2 = osiamConnector.createUser(user2, validAccessToken)

        then: 'the response elements should contain the expected email for each user'
        assert retUser1.emails != retUser2.emails
        assert retUser1.emails[0].value == retUser2.emails[0].value
        assert retUser1.emails[0].type != retUser2.emails[0].type
        assert retUser1.emails[0].primary == retUser2.emails[0].primary
    }

    def "REGT-OSNG-37: The token validation should not raise an exception in case of the OAuth2 client credentials grant because of missing user authentication"() {

        given: "a valid access token"
        AccessToken validAccessToken = osiamConnectorForClientCredentialsGrant.retrieveAccessToken()
        def responseStatusCode
        def responseContent

        when: "retrieving a user"
        new HTTPBuilder(RESOURCE_ENDPOINT).request(Method.GET, ContentType.JSON) { req ->
            uri.path = RESOURCE_ENDPOINT + "/Users"
            uri.query = [filter: 'userName eq "marissa"']
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContent = json
            }

            response.failure = { resp ->
                responseStatusCode = resp.statusLine.statusCode
            }
        }

        then: "the user should be retrieved without triggering an exception"
        responseStatusCode == 200
        responseContent.Resources[0].userName == 'marissa'
    }
    
    def 'OSNG-444: A request to revoke a valid token should invalidate the token'() {
    
    	given: 'a valid access token'
    	AccessToken accessToken = osiamConnectorForClientCredentialsGrant.retrieveAccessToken()
    	
    	when: 'a token revokation is performed'
    	AccessToken validationResult = osiamConnector.validateAccessToken(accessToken)
    	osiamConnector.revokeAccessToken(accessToken)
    	osiamConnector.validateAccessToken(accessToken) // authorization should now be invalid
    	
    	then: 'the token should be revoked'
    	validationResult.expired==false
    	thrown(UnauthorizedException)
    }
    
    def 'OSNG-444: A request to revoke an invalid token doesnt throw any exception'() {
        
        given: 'an invalid access token'
        AccessToken accessToken = new AccessToken.Builder("invalid").build()
        
        when: 'a token revokation is performed'
        osiamConnector.revokeAccessToken(accessToken)
        
        then: 'nothing should happen'
    }
    
    def 'OSNG-444: Multiple requests to revoke a valid token should invalidate the token'() {
        
        given: 'a valid access token'
        AccessToken accessToken = osiamConnectorForClientCredentialsGrant.retrieveAccessToken()
        
        when: 'multiple token revokations are performed'
        AccessToken validationResult = osiamConnector.validateAccessToken(accessToken)
        osiamConnector.revokeAccessToken(accessToken)
        osiamConnector.revokeAccessToken(accessToken)
        
        then: 'nothing should happen'
    }
}