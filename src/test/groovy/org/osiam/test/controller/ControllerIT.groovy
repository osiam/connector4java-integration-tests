package org.osiam.test.controller

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.dbunit.database.DatabaseDataSourceConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.osiam.client.oauth.AccessToken
import org.osiam.test.AbstractIT
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import spock.lang.Unroll

import javax.sql.DataSource

/**
 * Base class for controller integration tests.
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
        testCase | requestPath  | contentType        | expectedResponseCode | expectedResponseType
        "a"      | "/Users"     | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "b"      | "/Users/"    | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "c"      | "/Groups"    | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "d"      | "/Groups/"   | ContentType.JSON   | 200                  | "application/json;charset=UTF-8"
        "e"      | "/Users"     | ContentType.ANY    | 200                  | "application/json;charset=UTF-8"
        "f"      | "/Users"     | ContentType.TEXT   | 406                  | null
        "g"      | "/Users"     | ContentType.BINARY | 406                  | null
        "h"      | "/Users"     | ContentType.HTML   | 406                  | null
        "i"      | "/Users"     | ContentType.URLENC | 406                  | null
        "j"      | "/Users"     | ContentType.XML    | 406                  | null
        "k"      | "/Users"     | "invalid"          | 406                  | null
        "l"      | "/Users"     | "/"                | 406                  | null
    }

    @Unroll
    def "REGT-002-#testCase: A search operation on the Users endpoint with search string #searchString should return HTTP status code #expectedResponseCode."() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCode
        def responseContentType
        def responseFailureText

        http.request(Method.GET, ContentType.JSON) { req ->
            uri.path = OSIAM_ENDPOINT + "/Users"
            uri.query = [ filter:searchString ]
            headers."Authorization" = "Bearer " + validAccessToken.getToken()

            // response handler for a success response code:
            response.success = { resp, json ->
                responseStatusCode = resp.statusLine.statusCode
                responseContentType = resp.headers."Content-Type"
            }

            // handler for any failure status code:
            response.failure = { resp, json ->
                responseFailureText = json
                responseStatusCode = resp.statusLine.statusCode
                contentType = resp.headers."Content-Type"
            }

        }

        then: "the response should be as expected"
        assert responseStatusCode == expectedResponseCode

        expect: "the response text should be as expected"
        assert responseFailureText == expectedResponseFailureText

        where:
        testCase | searchString          | expectedResponseCode | expectedResponseFailureText
        "a"      | "userName eq marissa" | 200                  | null                                                                                              // String
        "b"      | "userName co ari"     | 200                  | null                                                                                              // String
        "c"      | "userName sw mar"     | 200                  | null                                                                                              // String
        "d"      | "userName pr"         | 200                  | null                                                                                              // String
        "e"      | "userName gt l"       | 200                  | null                                                                                              // String
        "f"      | "userName ge m"       | 200                  | null                                                                                              // String
        "g"      | "userName lt n"       | 200                  | null                                                                                              // String
        "h"      | "userName le m"       | 200                  | null                                                                                              // String
        "i"      | "emails.type eq work" | 200                  | null                                                                                              // Enum
        "j"      | "emails.type co work" | 409                  | "[error_code:CONFLICT, description:String filter operator is not applicable on Enum type.]"       // Enum
        "k"      | "emails.type sw rk"   | 409                  | "[error_code:CONFLICT, description:String filter operator is not applicable on Enum type.]"       // Enum
        "l"      | "emails.type pr work" | 200                  | null                                                                                              // Enum
        "m"      | "emails.type gt work" | 200                  | null                                                                                              // Enum
        "n"      | "emails.type ge work" | 200                  | null                                                                                              // Enum
        "o"      | "emails.type lt work" | 200                  | null                                                                                              // Enum
        "p"      | "emails.type le work" | 200                  | null                                                                                              // Enum
        "q"      | "active eq true"      | 200                  | null                                                                                              // boolean
        "r"      | "active co true"      | 409                  | "[error_code:CONFLICT, description:String filter operator is not applicable on Boolean type.]"    // boolean
        "s"      | "active sw true"      | 409                  | "[error_code:CONFLICT, description:String filter operator is not applicable on Boolean type.]"    // boolean
        "t"      | "active pr true"      | 200                  | null                                                                                              // boolean
        "u"      | "active gt true"      | 200                  | null                                                                                              // boolean
        "v"      | "active ge true"      | 200                  | null                                                                                              // boolean
        "w"      | "active lt true"      | 200                  | null                                                                                              // boolean
        "x"      | "active le true"      | 200                  | null                                                                                              // boolean
    }
}
