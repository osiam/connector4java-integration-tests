package de.osiam.client;

import static org.springframework.test.util.AssertionErrors.fail;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;

import org.junit.Before;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;

public abstract class AbstractIntegrationTestBase {
    static final protected String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    static final protected String INVALID_ID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    static final protected String INVALID_STRING = "invalid";
    static final protected String DELETE_USER_ID = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
    static final protected String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected String endpointAddress = "http://localhost:8180/osiam-server";
    protected String clientId = "example-client";
    protected String clientSecret = "secret";
    protected OsiamConnector oConnector;
    protected AccessToken accessToken;

    @Before
    public void abstractSetUp() throws Exception {

        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(endpointAddress).
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.PASSWORD).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }
    
    protected void givenAnAccessTokenForOneSecond() throws Exception {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(endpointAddress).
                setClientId("example-client-2").
                setClientSecret("secret1").
                setGrantType(GrantType.PASSWORD).
                setUserName("hsimpson").
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

    protected int expectedNumberOfMembers(int expectedMembers) {

        return expectedMembers;
    }

    protected String encodeExpected(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            fail("Unable to encode queryString");
        }
        return ""; //can't reach
    }

}