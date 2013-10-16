package org.osiam.test

import org.osiam.client.connector.OsiamConnector
import org.osiam.client.oauth.AccessToken
import org.osiam.client.oauth.GrantType
import org.osiam.client.oauth.Scope
import spock.lang.Specification

/**
 * Base class for integration tests.
 *
 * @author: Andreas Grau, tarent solutions GmbH, 10.09.13
 * @version: 1.0
 */
abstract class AbstractIT extends Specification {

    private static final String CLIENT_ID = "example-client"
    private static final String CLIENT_SECRET = "secret"

    private static final String USER_NAME = "marissa"
    private static final String USER_PASSWORD = "koala"

    protected static final String AUTH_ENDPOINT = "http://localhost:8180/osiam-auth-server"
    protected static final String RESOURCE_ENDPOINT = "http://localhost:8180/osiam-resource-server"

    protected OsiamConnector osiamConnector;
    protected AccessToken accessToken;

    def setup() {
        OsiamConnector.Builder connectorBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT).
                setResourceEndpoint(RESOURCE_ENDPOINT).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName(USER_NAME).
                setPassword(USER_PASSWORD).
                setScope(Scope.ALL)
        osiamConnector = connectorBuilder.build()
    }
}