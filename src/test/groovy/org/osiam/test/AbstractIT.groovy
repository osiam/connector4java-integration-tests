package org.osiam.test

import org.dbunit.database.DatabaseDataSourceConnection
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.dbunit.operation.DatabaseOperation
import org.osiam.client.connector.OsiamConnector
import org.osiam.client.oauth.AccessToken
import org.osiam.client.oauth.GrantType
import org.osiam.client.oauth.Scope
import org.springframework.context.ApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import spock.lang.Specification

import javax.sql.DataSource

/**
 * Base class for integration tests.
 *
 * @author: Andreas Grau, tarent solutions GmbH, 10.09.13
 * @version: 1.0
 */
abstract class AbstractIT extends Specification {

    protected static final String CLIENT_ID = "example-client"
    private static final String CLIENT_SECRET = "secret"

    private static final String USER_NAME = "marissa"
    private static final String USER_PASSWORD = "koala"

    protected static final String AUTH_ENDPOINT = "http://localhost:8180/osiam-auth-server"
    protected static final String RESOURCE_ENDPOINT = "http://localhost:8180/osiam-resource-server"

    protected OsiamConnector osiamConnector;
    protected AccessToken accessToken;

    protected OsiamConnector osiamConnectorForClientCredentialsGrant;

    def setup() {
        osiamConnector = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT).
                setResourceEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName(USER_NAME).
                setPassword(USER_PASSWORD).
                setScope(Scope.ALL).build()

        osiamConnectorForClientCredentialsGrant = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT).
                setResourceEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL).build()
    }


    def setupDatabase(String seedXmlName) {
        // Load Spring context configuration.
        ApplicationContext ac = new ClassPathXmlApplicationContext("context.xml")
        // Get dataSource configuration.
        DataSource dataSource = (DataSource) ac.getBean("dataSource")
        // Establish database connection.
        IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource)
        // Load the initialization data from file.
        IDataSet initData = new FlatXmlDataSetBuilder().build(ac.getResource(seedXmlName).getFile())

        // Insert initialization data into database.
        try {
            //Deletes all tables before inserting maybe smaller seed, to avoid constraint violations
            DatabaseOperation.DELETE_ALL.execute(connection, new FlatXmlDataSetBuilder().build(ac.getResource("database_seed.xml").getFile()))
            DatabaseOperation.INSERT.execute(connection, initData)
        }
        finally {
            connection.close();
        }
    }
}