package de.osiam.client;

import org.junit.Before;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.AuthService;
import org.osiam.client.oauth.GrantType;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;

import static org.springframework.test.util.AssertionErrors.fail;

public abstract class AbstractIntegrationTestBase {
    static final protected String VALID_USER_UUID = "834b410a-943b-4c80-817a-4465aed037bc";
    static final protected String INVALID_UUID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    static final protected String INVALID_STRING = "invalid";
    static final protected String DELETE_USER_UUID = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
    static final protected String VALID_GROUP_UUID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected String endpointAddress = "http://localhost:8080/osiam-server";
    protected String clientId = "example-client";
    protected String clientSecret = "secret";
    protected AuthService authService;
    protected AccessToken accessToken;

    @Before
    public void abstractSetUp() throws Exception {

        AuthService.Builder authBuilder = new AuthService.Builder(endpointAddress).
                clientId(clientId).
                clientSecret(clientSecret).
                grantType(GrantType.PASSWORD).
                username("marissa").
                password("koala");
        authService = authBuilder.build();
        accessToken = authService.retrieveAccessToken();
    }
    
    protected void givenAnAccessTokenForOneSecond() throws Exception {
        AuthService.Builder authBuilder = new AuthService.Builder(endpointAddress).
                clientId("example-client-2").
                clientSecret("secret1").
                grantType(GrantType.PASSWORD).
                username("hsimpson").
                password("koala");
        authService = authBuilder.build();
        accessToken = authService.retrieveAccessToken();
    }

    protected void givenAnInvalidAccessToken() throws Exception {
        accessToken = new AccessToken();
        Field tokenField = accessToken.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(accessToken, AbstractIntegrationTestBase.INVALID_UUID);
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