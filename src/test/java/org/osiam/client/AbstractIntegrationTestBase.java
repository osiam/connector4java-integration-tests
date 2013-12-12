package org.osiam.client;

import org.junit.Before;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;

import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractIntegrationTestBase {
    protected static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    protected static final String INVALID_ID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    protected static final String INVALID_STRING = "invalid";
    protected static final String DELETE_USER_ID = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
    protected static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    protected static final String CLIENT_ID = "example-client";
    protected static final String CLIENT_SECRET = "secret";
    protected OsiamConnector oConnector;
    protected AccessToken accessToken;


    @Before
    public void abstractSetUp() throws Exception {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }

    protected void givenAnAccessTokenForOneSecond() throws Exception {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId("short-living-client").
                setClientSecret("other-secret").
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }

    protected void givenAnInvalidAccessToken() throws Exception {
        accessToken = new AccessToken();
        Field tokenField = accessToken.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(accessToken, AbstractIntegrationTestBase.INVALID_ID);
        tokenField.setAccessible(false);
    }

    protected String encodeExpected(String string) {
        String encoded = null;
        try {
            encoded = URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("Unable to encode queryString");
        }
        return encoded;
    }

}