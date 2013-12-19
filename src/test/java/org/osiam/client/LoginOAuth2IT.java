package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
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
                .setClientId(CLIENT_ID).setClientSecret(CLIENT_SECRET)
                .setClientRedirectUri(REDIRECT_URI)
                .setGrantType(GrantType.AUTHORIZATION_CODE).setScope(Scope.ALL)
                .build();

        loginUri = oConnector.getRedirectLoginUri();
        defaultHttpClient = new DefaultHttpClient();
    }

    @Test
    public void test_successful_login() throws IOException {
        givenValidAuthCode();
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    public void login_and_get_me_user() throws IOException {
        givenValidAuthCode();
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        User user = oConnector.getCurrentUser(accessToken);
        assertEquals("marissa", user.getUserName());
    }

    @Test
    public void test_successful_login_while_using_httpResponse() throws IOException {
        givenValidAuthCode();
        givenAuthCode();
        givenAccessTokenUsingHttpResponse();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test(expected = ConflictException.class)
    public void getting_acces_token_two_times_raises_exception() throws IOException {
        givenValidAuthCode();
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

    private void givenAccessTokenUsingAuthCode() {
        accessToken = oConnector.retrieveAccessToken(authCode);
    }

    private void givenAccessTokenUsingHttpResponse() {
        accessToken = oConnector.retrieveAccessToken(authCodeResponse);
    }

    private void givenValidAuthCode() throws IOException {
        String currentRedirectUri;

        {
            HttpGet httpGet = new HttpGet(loginUri);
            defaultHttpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/login.do");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("j_username", "marissa"));
            loginCredentials.add(new BasicNameValuePair("j_password", "koala"));
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
                    AUTH_ENDPOINT_ADDRESS + "/login.do");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("j_username", "marissa"));
            loginCredentials.add(new BasicNameValuePair("j_password", "koala"));
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
