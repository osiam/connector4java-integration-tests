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

        when: "a valid request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)

        def responseStatusCode
        def responseContentType

        http.request(scope, contentType) { req ->
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

        then: "the response should be 200 OK"
        assert responseStatusCode == expectedResponseCode

        expect: "the response type is JSON"
        assert responseContentType == expectedResponseType

        where:
        testCase   | scope       | contentType        | expectedResponseCode | expectedResponseType             | requestPath
        "a"        | Method.GET  | ContentType.JSON   | 200                  | "application/json;charset=UTF-8" | "/Users"
        "b"        | Method.GET  | ContentType.JSON   | 200                  | "application/json;charset=UTF-8" | "/Users/"
        "c"        | Method.GET  | ContentType.JSON   | 200                  | "application/json;charset=UTF-8" | "/Groups"
        "d"        | Method.GET  | ContentType.JSON   | 200                  | "application/json;charset=UTF-8" | "/Groups/"
        "e"        | Method.GET  | ContentType.ANY    | 200                  | "application/json;charset=UTF-8" | "/Users"
        "f"        | Method.GET  | ContentType.TEXT   | 406                  | null                             | "/Users"
        "g"        | Method.GET  | ContentType.BINARY | 406                  | null                             | "/Users"
        "h"        | Method.GET  | ContentType.HTML   | 406                  | null                             | "/Users"
        "i"        | Method.GET  | ContentType.URLENC | 406                  | null                             | "/Users"
        "j"        | Method.GET  | ContentType.XML    | 406                  | null                             | "/Users"
        "k"        | Method.GET  | "invalid"          | 406                  | null                             | "/Users"
        "l"        | Method.GET  | "/"                | 406                  | null                             | "/Users"
    }
}
