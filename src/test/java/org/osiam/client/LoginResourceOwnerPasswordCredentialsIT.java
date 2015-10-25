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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.BadCredentialsException;
import org.osiam.client.exception.ConnectionInitializationException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for resource owner password credentials grant.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed_login_password_credentials.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class LoginResourceOwnerPasswordCredentialsIT extends AbstractIntegrationTestBase {

    @Autowired
    private DataSource dataSource;

    @Test
    public void login_with_resource_owner_password_credentials_grant_should_provide_an_refresh_token() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        assertNotNull("The hole access token object was null.", accessToken);
        assertNotNull("The access token was null.", accessToken.getToken());
        assertNotNull("The refresh token was null.", accessToken.getRefreshToken());
    }

    @Test
    @DatabaseSetup("/database_seed_login_with_old_hash.xml")
    public void login_updates_to_bcrypt_hash() throws SQLException {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa02", "koala", Scope.ADMIN);
        String sql = "SELECT password FROM scim_user WHERE internal_id = 100005";
        ResultSet rs = dataSource.getConnection().createStatement().executeQuery(sql);
        rs.next();
        final String bCryptHashedPassword = rs.getString(1);
        assertThat(bCryptHashedPassword, startsWith("$2a$13"));
        assertThat(bCryptHashedPassword, not(equalTo
                ("fb0ae3c36077b77c1907876f94a8ac65559454879b9750197e69a9325431a6e2d588f587c641a5a91e90b9f26ade1294d921ce3d5a5842e77bdc27b6f8d3146d")));
    }

    @Test(expected = BadCredentialsException.class)
    public void retrieve_access_token_with_wrong_credentials_throws_bad_credentials() {
        OSIAM_CONNECTOR.retrieveAccessToken("marissa", "wrong", Scope.ADMIN);
    }

    @Ignore
    @Test
    public void login_with_two_users_works() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        AccessToken at2 = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        assertNotNull(accessToken);
        assertNotNull(at2);
    }

    @Ignore
    @Test
    public void multiple_failed_logins() {
        for (int i = 0; i < 3; i++) {
            try {
                OSIAM_CONNECTOR.retrieveAccessToken("marissa03", "wrongPassword", Scope.ADMIN);
            } catch (ConnectionInitializationException e) {
                assertTrue(e.getMessage().contains("Bad credentials"));
            }
        }

        try {
            OSIAM_CONNECTOR.retrieveAccessToken("marissa03", "koala", Scope.ADMIN);
        } catch (ConnectionInitializationException e) {
            assertTrue(e.getMessage().contains("temporary locked"));
        }
    }

    @Ignore
    @Test
    public void multiple_failed_logins_reset() {
        for (int i = 0; i < 2; i++) {
            try {
                OSIAM_CONNECTOR.retrieveAccessToken("marissa04", "wrongPassword", Scope.ADMIN);
            } catch (ConnectionInitializationException e) {
                assertTrue(e.getMessage().contains("Bad credentials"));
            }
        }

        OSIAM_CONNECTOR.retrieveAccessToken("marissa04", "koala", Scope.ADMIN);

        for (int i = 0; i < 2; i++) {
            try {
                OSIAM_CONNECTOR.retrieveAccessToken("marissa04", "wrongPassword", Scope.ADMIN);
            } catch (ConnectionInitializationException e) {
                assertTrue(e.getMessage().contains("Bad credentials"));
            }
        }
        OSIAM_CONNECTOR.retrieveAccessToken("marissa04", "koala", Scope.ADMIN);
    }

    @Ignore
    @Test
    public void multiple_failed_logins_wait() {
        for (int i = 0; i < 3; i++) {
            try {
                OSIAM_CONNECTOR.retrieveAccessToken("marissa05", "wrongPassword", Scope.ADMIN);
            } catch (ConnectionInitializationException e) {
                assertTrue(e.getMessage().contains("Bad credentials"));
            }
        }

        try {
            Thread.sleep(3000);
            OSIAM_CONNECTOR.retrieveAccessToken("marissa05", "koala", Scope.ADMIN);
        } catch (InterruptedException e) {
        }
    }
}
