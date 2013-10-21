package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class RegressionErrorTrigger {

    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    private final String loginUri = AUTH_ENDPOINT_ADDRESS + "/oauth/authorize?client_id=example-client&response_type=code&redirect_uri=http%3A%2F%2Flocalhost%3A5000%2Foauth2&scope=GET+POST+PUT+PATCH+DELETE";
    private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();

    @Test
    @Ignore("Not in use at this point")
    public void aproval_is_triggered_out_of_context() throws Exception {
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
            HttpResponse httpResponse = defaultHttpClient.execute(httpPost);

            httpPost.releaseConnection();
            assertFalse(httpResponse.getStatusLine().getStatusCode() == 500);
        }
    }
}
