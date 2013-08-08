package de.osiam.client;

import org.junit.Before;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.AuthService;
import org.osiam.client.oauth.GrantType;

import java.lang.reflect.Field;
import java.util.UUID;

public abstract class AbstractIntegrationTestBase {
    static final protected String VALID_USER_UUID = "834b410a-943b-4c80-817a-4465aed037bc";
    static final protected String INVALID_UUID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    protected String endpointAddress = "http://localhost:8080/osiam-server";
    protected String clientId = "example-client";
    protected String clientSecret = "secret";
    protected AuthService authService;
    protected AccessToken accessToken;

    @Before
    public void abstractSetUp() throws Exception {

        AuthService.Builder authBuilder = new AuthService.Builder(endpointAddress).
                withClientId(clientId).
                withClientSecret(clientSecret).
                withGrantType(GrantType.PASSWORD).
                withUsername("marissa").
                withPassword("koala");
        authService = authBuilder.build();
        accessToken = authService.retrieveAccessToken();
    }

    protected void given_an_invalid_access_token() throws Exception {
        accessToken = new AccessToken();
        Field tokenField = accessToken.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(accessToken, AbstractIntegrationTestBase.INVALID_UUID);
        tokenField.setAccessible(false);
    }

}