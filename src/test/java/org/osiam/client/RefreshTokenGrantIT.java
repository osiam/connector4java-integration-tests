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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seeds/RefreshTokenGrantIT/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class RefreshTokenGrantIT extends AbstractIntegrationTestBase {

    @Test
    public void refreshing_an_access_token_should_provide_a_new_access_token() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        assertNotNull("The access token was null.", accessToken.getToken());
        assertNotNull("The refresh token was null.", accessToken.getRefreshToken());

        // Refresh the previously token with the retrieved refresh token
        AccessToken accessTokenRF = OSIAM_CONNECTOR.refreshAccessToken(accessToken);

        assertNotNull("The access token after refresh was null.", accessTokenRF.getToken());
        assertNotNull("The refresh token after refresh was null.", accessTokenRF.getRefreshToken());

        // Check the the refresh token is equal and the access token is a new one
        assertEquals("The refresh tokens were not equal.",
                accessToken.getRefreshToken(),
                accessTokenRF.getRefreshToken());
        assertNotEquals("The access tokens were equal.", accessToken.getToken(), accessTokenRF.getToken());
    }

    @Test
    public void refreshing_an_access_token_when_expired() throws InterruptedException {
        OsiamConnector connector = new OsiamConnector.Builder()
                .withEndpoint(OSIAM_ENDPOINT)
                .setClientId("short-living-client")
                .setClientSecret(CLIENT_SECRET)
                .setClientRedirectUri("http://localhost:5000/oauth2")
                .build();
        AccessToken shortLivingAccessToken = connector.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        waitForAccessTokenToExpire(shortLivingAccessToken);

        // Refresh the previously token with the retrieved refresh token
        AccessToken accessTokenRF = connector.refreshAccessToken(shortLivingAccessToken);

        assertNotNull("The access token after refresh is null.", accessTokenRF.getToken());
        assertNotNull("The refresh token after refresh is null.", accessTokenRF.getRefreshToken());
        assertEquals("The refresh tokens are not equal.",
                shortLivingAccessToken.getRefreshToken(),
                accessTokenRF.getRefreshToken());
        assertNotEquals("The access tokens are equal.", shortLivingAccessToken.getToken(), accessTokenRF.getToken());
    }

    private void waitForAccessTokenToExpire(final AccessToken shortLivingAccessToken) throws InterruptedException {
        int tries = 0;
        while (tries <= 100 && !shortLivingAccessToken.isExpired()) {
            tries++;
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }
}
