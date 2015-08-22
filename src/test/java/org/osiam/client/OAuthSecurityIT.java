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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
public class OAuthSecurityIT extends AbstractIntegrationTestBase {

    @Test(expected = NotAuthorizedException.class)
    public void client_is_secured() throws IOException {
        CLIENT.target(AUTH_ENDPOINT_ADDRESS + "/Client")
                .path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void users_is_secured() throws IOException {
        CLIENT.target(RESOURCE_ENDPOINT_ADDRESS + "/Users")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void groups_is_secured() throws IOException {
        CLIENT.target(RESOURCE_ENDPOINT_ADDRESS + "/Groups")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }

    @Test(expected = NotAuthorizedException.class)
    public void metrics_is_secured() throws IOException {
        CLIENT.target(RESOURCE_ENDPOINT_ADDRESS + "/Metrics")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }
}
