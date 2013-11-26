package org.osiam.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed_login_with_email_address_as_username.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class UserLoginWithEmailAddressAsUserNameIT extends AbstractIntegrationTestBase {

    @Test
    public void log_in_with_an_email_address_as_user_name_is_possible() {
        AccessToken at = getAccessToken("chunkylover53@aol.com", "koala");
        assertThat(at, is(notNullValue()));
    }

    private AccessToken getAccessToken(String userName, String password) {
        return new OsiamConnector.Builder()
                .setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId("example-client")
                .setClientSecret("secret")
                .setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS)
                .setUserName(userName)
                .setPassword(password)
                .setScope(Scope.ALL)
                .build()
                .retrieveAccessToken();
    }
}