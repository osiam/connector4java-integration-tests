package org.osiam.test.server

import groovyx.net.http.ContentEncoding
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.dbunit.database.DatabaseDataSourceConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.osiam.client.oauth.AccessToken
import org.osiam.resources.scim.Email
import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import org.osiam.resources.type.EmailType;
import org.osiam.test.AbstractIT
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import spock.lang.Unroll

import javax.sql.DataSource

/**
 * Base class for server integration tests.
 *
 * @author: Andreas Grau, tarent solutions GmbH, 10.09.13
 * @version: 1.0
 */
class ControllerIT extends AbstractIT {

    def setupSpec() {
        // Load Spring context configuration.
        ApplicationContext ac = new ClassPathXmlApplicationContext("context.xml")
        // Get dataSource configuration.
        DataSource dataSource = (DataSource) ac.getBean("dataSource")
        // Establish database connection.
        IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource)
        // Load the initialization data from file.
        IDataSet initData = new FlatXmlDataSetBuilder().build(ac.getResource("database_seed.xml").getFile())

        // Insert initialization data into database.
        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, initData)
        }
        finally {
            connection.close();
        }
    }

    @Unroll
    def "REGT-001-#testCase: An API request missing an accept header with scope #scope and content type #contentType on path #requestPath should return HTTP status code #expectedResponseCode and content type #expectedResponseType."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCode
        def responseContentType

        http.request(Method.GET, contentType) { req ->
            uri.path = OSIAM_ENDPOINT + requestPath
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

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
    }

    @Unroll
    def "REGT-002-#testCase: A search operation on the Users endpoint with search string #searchString should return HTTP status code #expectedResponseCode."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCode
        def responseContentType
        def responseErrorCode
        def responseFailureText


        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = OSIAM_ENDPOINT + "/Users"
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
                responseFailureText = json.description
                responseStatusCode = resp.statusLine.statusCode
                contentType = resp.headers."Content-Type"
            }

        }

        then: "the response and possible failure codes and text should be as expected"
        assert responseStatusCode == expectedResponseCode
        assert responseErrorCode == expectedResponseErrorCode
        assert responseFailureText == expectedResponseFailureText

        where:
        testCase | searchString                              | expectedResponseCode | expectedResponseErrorCode | expectedResponseFailureText
        "a"      | "userName eq marissa"                     | 200                  | null                      | null                                                                                            // String
        "b"      | "userName co ari"                         | 200                  | null                      | null                                                                                            // String
        "c"      | "userName sw mar"                         | 200                  | null                      | null                                                                                            // String
        "d"      | "userName pr"                             | 200                  | null                      | null                                                                                            // String
        "e"      | "userName gt l"                           | 200                  | null                      | null                                                                                            // String
        "f"      | "userName ge m"                           | 200                  | null                      | null                                                                                            // String
        "g"      | "userName lt n"                           | 200                  | null                      | null                                                                                            // String
        "h"      | "userName le m"                           | 200                  | null                      | null                                                                                            // String
        "i"      | "emails.type eq work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "j"      | "emails.type co work"                     | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'type'."                     // Enum (EmailEntity)
        "k"      | "emails.type sw work"                     | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'type'."                     // Enum (EmailEntity)
        "l"      | "emails.type pr work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "m"      | "emails.type gt work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "n"      | "emails.type ge work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "o"      | "emails.type lt work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "p"      | "emails.type le work"                     | 200                  | null                      | null                                                                                            // Enum (EmailEntity)
        "q"      | "active eq true"                          | 200                  | null                      | null                                                                                            // boolean
        "r"      | "active co true"                          | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'active' of type 'Boolean'." // boolean
        "s"      | "active sw true"                          | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'active' of type 'Boolean'." // boolean
        "t"      | "active pr true"                          | 200                  | null                      | null                                                                                            // boolean
        "u"      | "active gt true"                          | 200                  | null                      | null                                                                                            // boolean
        "v"      | "active ge true"                          | 200                  | null                      | null                                                                                            // boolean
        "w"      | "active lt true"                          | 200                  | null                      | null                                                                                            // boolean
        "x"      | "active le true"                          | 200                  | null                      | null                                                                                            // boolean
        "y"      | "meta.created co 2013-08-08T19:46:20.638" | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'created'."                  // Date
        "z"      | "meta.created sw 2013-08-08T1"            | 409                  | "CONFLICT"                | "String filter operators 'co' and 'sw' are not applicable on field 'created'."                  // Date
    }

    def "REGT-005: A search filter String matching two users should return totalResults=2 and two unique Resource elements."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a filter request matching two users is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCode
        def responseContent

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = OSIAM_ENDPOINT + "/Users"
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

    def "REGT-OSNG-141: E-Mail address should not be unique. So two different users should be able to add the same address and getting displayed only the own entry."() {

        given: "a valid access token and two users with the same E-Mail address"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()
        def emailUserOne = new Email.Builder().setType(EmailType.WORK).setValue("sameMail@osiam.de").build()
        def emailUserTwo = new Email.Builder().setType(EmailType.HOME).setValue("sameMail@osiam.de").build()
        def user1 = new User.Builder("UserOne").setEmails([emailUserOne] as List).setExternalId("pew1").build()
        def user2 = new User.Builder("UserTwo").setEmails([emailUserTwo] as List).setExternalId("pew2").build()

        when: "a add user request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCodeUser1
        def responseStatusCodeUser2

        def responseContentUser1
        def responseContentUser2

        //Adding user one
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = OSIAM_ENDPOINT + "/Users"
            body = user1

            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCodeUser1 = resp.statusLine.statusCode
                responseContentUser1 = json
            }
        }
        //Adding user two
        http.request(Method.POST, ContentType.JSON) { req ->
            uri.path = OSIAM_ENDPOINT + "/Users"
            body = user2

            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            response.success = { resp, json ->
                responseStatusCodeUser2 = resp.statusLine.statusCode
                responseContentUser2 = json
            }
        }

        then: "the response elements should contain the expected email for each user"
        assert responseStatusCodeUser1 == 201
        assert responseStatusCodeUser2 == 201

        assert responseContentUser1.emails != responseContentUser2.emails
        assert responseContentUser1.emails[0].value == responseContentUser2.emails[0].value
        assert responseContentUser1.emails[0].type != responseContentUser2.emails[0].type
        assert responseContentUser1.emails[0].primary == responseContentUser2.emails[0].primary
    }
}