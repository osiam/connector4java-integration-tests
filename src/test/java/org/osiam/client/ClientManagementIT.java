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

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    private DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
    private static final String AUTHORIZATION = "Authorization";
    private static final String ACCEPT = "Accept";
    private static final String BEARER = "Bearer ";
    
    @Test
    public void get_client_by_id() throws ClientProtocolException, IOException {
        HttpGet httpGet = new HttpGet(AUTH_ENDPOINT_ADDRESS + "/Client/100003");
        httpGet.addHeader(AUTHORIZATION, BEARER + accessToken.getToken());
        httpGet.addHeader(ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
        CloseableHttpResponse response = defaultHttpClient.execute(httpGet);
        InputStream content = response.getEntity().getContent();
        String inputStreamStringValue = IOUtils.toString(content, "UTF-8");
        assertThat(inputStreamStringValue, containsString("error"));
        httpGet.releaseConnection();
    }
}