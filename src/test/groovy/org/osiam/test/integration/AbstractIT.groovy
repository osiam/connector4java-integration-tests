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
 */
abstract class AbstractIT extends Specification {

    protected static final String CLIENT_ID = "example-client"
    private static final String CLIENT_SECRET = "secret"

    private static final String USER_NAME = "marissa"
    private static final String USER_PASSWORD = "koala"

    private static final String USER_NAME_EMAIL_CHANGE = "GeorgeAlexander"
    private static final String USER_PASSWORD_EMAIL_CHANGE = "koala"

    protected static final String AUTH_ENDPOINT = "http://localhost:8180/osiam-auth-server"
    protected static final String RESOURCE_ENDPOINT = "http://localhost:8180/osiam-resource-server"
    protected static final String REGISTRATION_ENDPOINT = "http://localhost:8180/osiam-self-administration"

    protected OsiamConnector osiamConnector
    protected OsiamConnector osiamConnectorForClientCredentialsGrant
    protected OsiamConnector osiamConnectorForEmailChange

    def AccessToken accessToken

    def setupDatabase(String seedFileName) {

        // Load Spring context configuration.
        ApplicationContext ac = new ClassPathXmlApplicationContext("context.xml")
        // Get dataSource configuration.
        DataSource dataSource = (DataSource) ac.getBean("dataSource")
        // Establish database connection.
        IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource)
        // Load the initialization data from file.
        IDataSet initData = new FlatXmlDataSetBuilder().build(ac.getResource(seedFileName).getFile())

        // Insert initialization data into database.
        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, initData)
        }
        finally {
            connection.close()
        }

        osiamConnector = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT).
                setResourceServerEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName(USER_NAME).
                setPassword(USER_PASSWORD).
                setScope(Scope.ALL).build()

        osiamConnectorForClientCredentialsGrant = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT).
                setResourceServerEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL).build()

        osiamConnectorForEmailChange = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT).
                setResourceServerEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName(USER_NAME_EMAIL_CHANGE).
                setPassword(USER_PASSWORD_EMAIL_CHANGE).
                setScope(Scope.ALL).build()

        accessToken = osiamConnector.retrieveAccessToken()
    }

    def createAccessToken(def userName, def password) {
        OsiamConnector osiamConnector = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT).
                setResourceServerEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName(userName).
                setPassword(password).
                setScope(Scope.ALL).build()
        osiamConnector.retrieveAccessToken()
    }

    def cleanup() {
        // Load Spring context configuration.
        ApplicationContext ac = new ClassPathXmlApplicationContext("context.xml")
        // Get dataSource configuration.
        DataSource dataSource = (DataSource) ac.getBean("dataSource")
        // Establish database connection.
        IDatabaseConnection connection = new DatabaseDataSourceConnection(dataSource)
        // Load the initialization data from file.

        IDataSet initData = new FlatXmlDataSetBuilder().build(ac.getResource("database_tear_down.xml").getFile())

        // Insert initialization data into database.
        try {
            DatabaseOperation.DELETE_ALL.execute(connection, initData)
        }
        finally {
            connection.close()
        }
    }
}