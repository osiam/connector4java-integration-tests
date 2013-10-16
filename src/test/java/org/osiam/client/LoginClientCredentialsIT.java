package org.osiam.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class LoginClientCredentialsIT {

    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    protected String clientId = "example-client";
    protected String clientSecret = "secret";
    protected OsiamConnector oConnector;

	@Test
	public void login_with_client_credentials(){
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
	}

	@Test (expected = UnauthorizedException.class)
	public void login_with_wrong_client_credentials(){
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(clientId).
                setClientSecret("wrong" + clientSecret).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
	}

    @Test (expected = ConflictException.class)
    public void get_actual_user_rasies_exception(){
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        AccessToken accessToken = oConnector.retrieveAccessToken();
        oConnector.getMe(accessToken);
    }

}
