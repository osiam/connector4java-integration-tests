package org.osiam.test.controller

import groovyx.net.http.HTTPBuilder
import org.dbunit.database.DatabaseDataSourceConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.osiam.client.oauth.AccessToken
import org.osiam.test.AbstractIT
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext

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

    def "REGT-001: HTTP response codes and types"() {
        given: "a valid access token"
        AccessToken validAccessToken = osiamConnector.retrieveAccessToken()

        when: "a valid request is sent"
        def http = new HTTPBuilder(OSIAM_ENDPOINT)
        // TODO

        then: "the response should be 200 OK"
        // TODO
        assert false

        expect: "the response type is JSON"
        // TODO
    }
}