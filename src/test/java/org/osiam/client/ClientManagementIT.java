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
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
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
public class ClientManagementIT extends AbstractIntegrationTestBase {

    private static final String AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server/Client";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    
    Client client = ClientBuilder.newClient();

    @Test
    public void get_client_by_id() {
        String output = client.target(AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS)
            .path("example-client")
            .request(MediaType.APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getToken())
            .get(String.class);
        
        assertThat(output, containsString("example-client"));
    }

    @Test
    public void create_client() {
        String clientAsJsonString = "{\"id\":\"example-client-2\",\"accessTokenValiditySeconds\":2342,\"refreshTokenValiditySeconds\":2342,"
                + "\"redirectUri\":\"http://localhost:5055/oauth2\",\"client_secret\":\"secret-2\","
                + "\"scope\":[\"POST\",\"PATCH\",\"GET\",\"DELETE\",\"PUT\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\",\"password\"],"
                + "\"implicit\":false,\"validityInSeconds\":1337,\"expiry\":-3599000}";

        String response = client.target(AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS)
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + accessToken.getToken())
                .post(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON), String.class);
            

        assertThat(response, containsString("example-client-2"));
    }
    
    @Test
    public void delete_client() throws IOException {
        String deleteResponse = client.target(AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS)
                .path("short-living-client")
                .request()
                .header(AUTHORIZATION, BEARER + accessToken.getToken())
                .delete(String.class);
            
        assert(deleteResponse.isEmpty());

        Response response = client.target(AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS)
                .path("short-living-client")
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + accessToken.getToken())
                .get();
        
        InputStream content = (InputStream) response.getEntity();
        String inputStreamStringValue = IOUtils.toString(content, "UTF-8");
        assertThat(inputStreamStringValue, containsString("NOT_FOUND"));
    }
    
    @Test
    public void update_client() throws JSONException {
        String clientAsJsonString = "{\"id\":\"example-client\",\"accessTokenValiditySeconds\":1,\"refreshTokenValiditySeconds\":1,"
                + "\"redirectUri\":\"http://newhost:5000/oauth2\",\"client_secret\":\"secret\","
                + "\"scope\":[\"POST\",\"PATCH\",\"GET\",\"DELETE\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\"],"
                + "\"implicit\":true,\"validityInSeconds\":1}";

        String updated = client.target(AUTH_SERVER_CLIENT_ENDPOINT_ADDRESS)
                .path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, BEARER + accessToken.getToken())
                .put(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON), String.class);
        
        String expected = "{\"id\":\"example-client\",\"accessTokenValiditySeconds\":1,\"refreshTokenValiditySeconds\":1,"
                + "\"redirectUri\":\"http://newhost:5000/oauth2\",\"client_secret\":\"secret\","
                + "\"scope\":[\"POST\",\"PATCH\",\"GET\",\"DELETE\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\"],"
                + "\"implicit\":true,\"validityInSeconds\":1}";

        JSONAssert.assertEquals(expected, updated, false);
    }
}