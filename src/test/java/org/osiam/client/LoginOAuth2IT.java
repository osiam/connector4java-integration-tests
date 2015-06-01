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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Email.Type;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.UpdateUser;
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
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class LoginOAuth2IT extends AbstractIntegrationTestBase {

    private URI loginUri = OSIAM_CONNECTOR.getAuthorizationUri(Scope.ALL);
    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private String authCode;
    private AccessToken accessToken;
    private HttpResponse authCodeResponse;

    @Before
    public void before() {
        loginUri = OSIAM_CONNECTOR.getAuthorizationUri(Scope.ALL);
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_successful_login() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void the_approval_will_be_remembered() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();

        {
            HttpGet httpGet = new HttpGet(loginUri);
            httpGet.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.NETSCAPE);
            httpGet.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
            authCodeResponse = httpClient.execute(httpGet);
            httpGet.releaseConnection();
        }
        givenAuthCode();
        givenAccessTokenUsingAuthCode();

        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    @DatabaseSetup("/database_seeds/LoginOAuth2IT/database_seed_zero_validity.xml")
    public void the_approval_will_not_be_remembered_if_validity_is_zero() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();

        {
            HttpGet httpGet = new HttpGet(loginUri);
            httpGet.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.NETSCAPE);
            httpGet.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
            authCodeResponse = httpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        String response = IOUtils.toString(authCodeResponse.getEntity().getContent());
        assertThat(response, containsString("<title>Access confirmation</title>"));
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_successful_ldap_login() throws IOException {
        givenValidAuthCode("ben", "benspassword", "ldap");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        Query query = new QueryBuilder().filter("userName eq \"ben\"").build();
        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);
        User user = result.getResources().get(0);
        assertEquals(result.getTotalResults(), 1);
        assertEquals("ben", user.getUserName());
        assertEquals("Alex", user.getName().getFamilyName());
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_origin_is_set_successful_after_ldap_login() throws IOException {
        givenValidAuthCode("ben", "benspassword", "ldap");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        Query query = new QueryBuilder().filter("userName eq \"ben\"").build();
        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);
        User user = result.getResources().get(0);
        assertEquals(result.getTotalResults(), 1);
        assertEquals("ldap",
                user.getExtension("urn:org.osiam:scim:extensions:auth-server").getFieldAsString("origin"));
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_origin_is_not_set_after_internal_login() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        Query query = new QueryBuilder().filter("userName eq \"marissa\"").build();
        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);
        User user = result.getResources().get(0);
        assertEquals(result.getTotalResults(), 1);
        assertFalse(user.isExtensionPresent("urn:scim:schemas:osiam:2.0:authentication:server"));
    }

    @Test
    @Ignore("Fails mostly on Jenkins")
    @DatabaseSetup("/database_seed.xml")
    public void if_ldap_user_login_but_internal_user_already_exists_error_will_be_shown() throws IOException {
        String currentRedirectUri;
        String username = "marissa";
        String password = "koala";
        String provider = "ldap";

        {
            HttpGet httpGet = new HttpGet(loginUri);
            httpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(
                    AUTH_ENDPOINT_ADDRESS + "/login/check");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("username", username));
            loginCredentials.add(new BasicNameValuePair("password", password));
            loginCredentials.add(new BasicNameValuePair("provider", provider));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            HttpResponse response = httpClient.execute(httpPost);

            currentRedirectUri = response.getLastHeader("Location").getValue();

            httpPost.releaseConnection();
        }

        assertTrue(currentRedirectUri.contains("login/error"));

        {
            HttpGet httpGet = new HttpGet(currentRedirectUri);
            httpGet.setHeader("Accept-Language", "de-DE");
            HttpResponse response = httpClient.execute(httpGet);
            InputStream content = response.getEntity().getContent();
            String inputStreamStringValue = IOUtils.toString(content, "UTF-8");
            assertThat(inputStreamStringValue, containsString("Anmeldung über ldap nicht möglich"));
            httpGet.releaseConnection();
        }
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_successful_update_user_with_ldap_relogin() throws IOException, InterruptedException {
        final OsiamConnector connector = new OsiamConnector.Builder()
                .setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId("short-living-client")
                .setClientSecret("other-secret")
                .setClientRedirectUri("http://localhost:5001/oauth2")
                .build();

        accessToken = connector.retrieveAccessToken();

        loginUri = connector.getAuthorizationUri();

        givenValidAuthCode("ben", "benspassword", "ldap");
        givenAuthCode();
        accessToken = connector.retrieveAccessToken(authCode);

        Query query = new QueryBuilder().filter("userName eq \"ben\"").build();
        SCIMSearchResult<User> result = connector.searchUsers(query, accessToken);
        User user = result.getResources().get(0);

        Email ldapEmail = new Email.Builder().setValue("ben@ben.de").setType(new Type("ldap")).build();

        assertEquals(1, user.getEmails().size());
        assertTrue(user.getEmails().contains(ldapEmail));

        Email newEmail = new Email.Builder().setValue("ben@osiam.org").build();
        Email toBeDeleteLdapEmail = new Email.Builder().setValue("should.be@deleted.de").setType(new Type("ldap"))
                .build();
        UpdateUser updateUser = new UpdateUser.Builder().updateNickName("benNickname")
                .addEmail(newEmail).addEmail(toBeDeleteLdapEmail).deleteEmail(ldapEmail).build();

        connector.updateUser(user.getId(), updateUser, accessToken);

        Thread.sleep(1000);

        givenValidAuthCode("ben", "benspassword", "ldap");
        givenAuthCode();
        accessToken = connector.retrieveAccessToken(authCode);

        result = connector.searchUsers(query, accessToken);
        user = result.getResources().get(0);

        assertEquals(result.getTotalResults(), 1);
        assertEquals("ben", user.getUserName());
        assertEquals("Alex", user.getName().getFamilyName());
        assertEquals("benNickname", user.getNickName());
        assertEquals(2, user.getEmails().size());
        assertTrue(user.getEmails().contains(newEmail));
        assertTrue(user.getEmails().contains(ldapEmail));
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void login_and_get_me_user() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        User user = OSIAM_CONNECTOR.getCurrentUser(accessToken);
        assertEquals("marissa", user.getUserName());
    }

    @Test(expected = ConflictException.class)
    @DatabaseSetup("/database_seed.xml")
    public void getting_access_token_two_times_raises_exception() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        givenAccessTokenUsingAuthCode();
        fail("exception expected");
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_failure_login_when_client_not_set() throws IOException {
        givenValidAuthCode("marissa", "koala", "internal");
        givenAuthCode();
        givenAccessTokenUsingAuthCode();
        assertTrue(accessToken != null);
        assertNotNull(accessToken.getRefreshToken());
    }

    @Test
    @DatabaseSetup("/database_seed.xml")
    public void test_failure_login_when_user_not_active() throws IOException {
        String redirectUri = givenValidAuthCode("ewilley", "ewilley", "internal");
        assertTrue(accessToken == null);
        assertEquals(redirectUri, AUTH_ENDPOINT_ADDRESS + "/login/error");
    }

    private void givenAccessTokenUsingAuthCode() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken(authCode);
    }

    private String givenValidAuthCode(String username, String password, String provider) throws IOException {
        String currentRedirectUri;

        {
            HttpGet httpGet = new HttpGet(loginUri);
            httpClient.execute(httpGet);
            httpGet.releaseConnection();
        }

        {
            HttpPost httpPost = new HttpPost(AUTH_ENDPOINT_ADDRESS + "/login/check");

            List<NameValuePair> loginCredentials = new ArrayList<>();
            loginCredentials
                    .add(new BasicNameValuePair("username", username));
            loginCredentials.add(new BasicNameValuePair("password", password));
            loginCredentials.add(new BasicNameValuePair("provider", provider));
            UrlEncodedFormEntity loginCredentialsEntity = new UrlEncodedFormEntity(
                    loginCredentials, "UTF-8");

            httpPost.setEntity(loginCredentialsEntity);
            HttpResponse response = httpClient.execute(httpPost);

            currentRedirectUri = response.getLastHeader("Location").getValue();

            httpPost.releaseConnection();
        }

        {
            HttpGet httpGet = new HttpGet(currentRedirectUri);
            httpGet.getParams().setParameter(ClientPNames.COOKIE_POLICY,
                    CookiePolicy.NETSCAPE);
            httpGet.getParams().setBooleanParameter("http.protocol.handle-redirects", false);
            httpClient.execute(httpGet);
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
            authCodeResponse = httpClient.execute(httpPost);

            httpPost.releaseConnection();
        }
        return currentRedirectUri;
    }

    private void givenAuthCode() {
        Header header = authCodeResponse.getLastHeader("Location");
        if (header == null) {
            throw new RuntimeException("The Location Header is null");
        }
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
