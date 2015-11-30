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

package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConnectionInitializationException;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seed_groups.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class OsiamConnectorIT extends AbstractIntegrationTestBase {

    private static OsiamConnector lowTimeoutConnector;

    @BeforeClass
    public static void before() {
        lowTimeoutConnector = new OsiamConnector.Builder()
                .withEndpoint(OSIAM_ENDPOINT)
                .setClientId("example-client")
                .setClientSecret("secret")
                .withConnectTimeout(1)
                .withReadTimeout(1)
                .build();
    }

    @Test
    public void when_combined_endpoint_is_set_both_endpoints_are_valid() {
        final OsiamConnector osiamConnector = new OsiamConnector.Builder()
                .withEndpoint(OSIAM_ENDPOINT)
                .setClientId("example-client")
                .setClientSecret("secret")
                .build();
        accessToken = osiamConnector.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        assertNotNull(osiamConnector.getUser(VALID_USER_ID, accessToken));
        assertNotNull(osiamConnector.getGroup(VALID_GROUP_ID, accessToken));
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_retrieving_user_access_token_triggers_exception() {
        lowTimeoutConnector.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_refresh_access_token_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.refreshAccessToken(accessToken, Scope.ADMIN);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_retrieving_client_access_token_triggers_exception() {
        lowTimeoutConnector.retrieveAccessToken(Scope.ADMIN);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_validating_access_token_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.validateAccessToken(accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_revoking_access_token_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.revokeAccessToken(accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_revoking_all_access_tokens_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.revokeAllAccessTokens(VALID_USER_ID, accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_retrieving_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.getUser(VALID_USER_ID, accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_retrieving_current_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.getCurrentUserBasic(accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_search_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.searchUsers(new QueryBuilder().build(), accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_create_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.searchUsers(new QueryBuilder().build(), accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_update_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.updateUser(VALID_USER_ID, new UpdateUser.Builder().build(), accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_replace_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.replaceUser(VALID_USER_ID, new User.Builder().build(), accessToken);
    }

    @Test(expected = ConnectionInitializationException.class)
    public void setting_timeout_to_minimum_and_delete_user_triggers_exception() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        lowTimeoutConnector.deleteUser(VALID_USER_ID, accessToken);
    }
}
