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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.ForbiddenException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class LoginOAuth2IT {

    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    private static String CLIENT_ID = "example-client";
    private static String CLIENT_SECRET = "secret";
    private static String REDIRECT_URI = "http://localhost:5000/oauth2";
    private OsiamConnector oConnector;
    private URI loginUri;
    private DefaultHttpClient defaultHttpClient;
    private String authCode;
    private AccessToken accessToken;
    private HttpResponse authCodeResponse;

    @Before
    public void setUp() throws Exception {
        oConnector = new OsiamConnector.Builder()
                .setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setClientRedirectUri(REDIRECT_URI)
                .setGrantType(GrantType.AUTHORIZATION_CODE).setScope(Scope.ALL)
                .build();

        loginUri = oConnector.getRedirectLoginUri();
        defaultHttpClient = new DefaultHttpClient();
    }

    @Test
    public void test_successful_login() throws IOException {
        givenValidAuthCode("marissa", "koala");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    public void login_and_get_me_user() throws IOException {
        givenValidAuthCode("marissa", "koala");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        User user = oConnector.getCurrentUser(accessToken);
        assertEquals("marissa", user.getUserName());
    }

    @Test
    public void test_successful_login_while_using_httpResponse() throws IOException {
        givenValidAuthCode("marissa", "koala");
        givenAuthCode();
        givenAccessTokenUsingHttpResponse();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test(expected = ConflictException.class)
    public void getting_acces_token_two_times_raises_exception() throws IOException {
        givenValidAuthCode("marissa", "koala");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        givenAccessTokenUsingAuthCode();
        fail("exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void user_denied_recognized_correctly() throws IOException {
        givenDenyResponse();
        givenAccessTokenUsingHttpResponse();
        fail("exception expected");
    }

    @Test
    public void test_failure_login_when_client_not_set() throws IOException {
        givenValidAuthCode("marissa", "koala");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }
    
    @Test
    public void test_failure_login_when_user_not_active() throws IOException {
        String redirectUri = givenValidAuthCode("ewilley", "ewilley");
        assertTrue(accessToken == null);
        assertEquals(redirectUri, AUTH_ENDPOINT_ADDRESS + "/login/error");
    }
    
    private void givenAccessTokenUsingAuthCode() {
        accessToken = oConnector.retrieveAccessToken(authCode);
    }

    private void givenAccessTokenUsingHttpResponse() {
        accessToken = oConnector.retrieveAccessToken(authCodeResponse);
    }

    private String givenValidAuthCode(String username, String password) throws IOException {
        String currentRedirectUri;

        {
            HttpGet httpGet = new HttpGet(loginUri);
            defaultHttpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/login/check");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("username", username));
            loginCredentials.add(new BasicNameValuePair("password", password));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            HttpResponse response = defaultHttpClient.execute(httpPost);

            currentRedirectUri = response.getLastHeader("Location").getValue();

            httpPost.releaseConnection();
        }

        {
            HttpGet httpGet = new HttpGet(currentRedirectUri);
            httpGet.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.NETSCAPE);
            httpGet.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
            defaultHttpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/oauth/authorize");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials.add(new BasicNameValuePair("user_oauth_approval",
                    "true"));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            authCodeResponse = defaultHttpClient.execute(httpPost);

            httpPost.releaseConnection();
        }
        return currentRedirectUri;
    }

    private void givenDenyResponse() throws IOException {
        String currentRedirectUri;

        {
            HttpGet httpGet = new HttpGet(loginUri);
            defaultHttpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/login/check");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("username", "marissa"));
            loginCredentials.add(new BasicNameValuePair("password", "koala"));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            HttpResponse response = defaultHttpClient.execute(httpPost);

            currentRedirectUri = response.getLastHeader("Location").getValue();

            httpPost.releaseConnection();
        }

        {
            HttpGet httpGet = new HttpGet(currentRedirectUri);
            defaultHttpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/oauth/authorize");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials.add(new BasicNameValuePair("user_oauth_approval",
                    "false"));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            authCodeResponse = defaultHttpClient.execute(httpPost);

            httpPost.releaseConnection();
        }
    }

    private void givenAuthCode() {
        Header header = authCodeResponse.getLastHeader("Location");
        HeaderElement[] elements = header.getElements();
        for (HeaderElement actHeaderElement : elements) {
            if (actHeaderElement.getName().contains("code")) {
                authCode = actHeaderElement.getValue();
                break;
            }
            if (actHeaderElement.getName().contains("error")) {
                throw new Error("The user had denied the acces to his data.");
            }
        }
        if (authCode == null) {
            throw new Error("Could not find any auth code or error message in the given Response");
        }
    }

}
