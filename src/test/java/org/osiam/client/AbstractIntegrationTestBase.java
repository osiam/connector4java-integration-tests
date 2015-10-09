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

import org.glassfish.jersey.jackson.JacksonFeature;
import org.joda.time.format.ISODateTimeFormat;
import org.osiam.client.oauth.AccessToken;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public abstract class AbstractIntegrationTestBase {

    protected static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    protected static final String INVALID_ID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    protected static final String INVALID_STRING = "invalid";
    protected static final String DELETE_USER_ID = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
    protected static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected static final String OSIAM_HOST;
    protected static final String AUTH_ENDPOINT_ADDRESS;
    protected static final String RESOURCE_ENDPOINT_ADDRESS;
    protected static final String CLIENT_ID = "example-client";
    protected static final String CLIENT_SECRET = "secret";
    protected static final OsiamConnector OSIAM_CONNECTOR;
    protected static final Client CLIENT = ClientBuilder.newBuilder().register(JacksonFeature.class).build();

    static {
        OsiamConnector.setConnectTimeout(Integer.parseInt(System.getProperty("connector.timeout", "-1")));
        OsiamConnector.setReadTimeout(Integer.parseInt(System.getProperty("connector.timeout", "-1")));

        OSIAM_HOST = System.getProperty("osiam.test.host", "http://localhost:8180");
        AUTH_ENDPOINT_ADDRESS = OSIAM_HOST + "/osiam-auth-server";
        RESOURCE_ENDPOINT_ADDRESS = OSIAM_HOST + "/osiam-resource-server";

        OSIAM_CONNECTOR = new OsiamConnector.Builder()
                .setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setClientRedirectUri("http://localhost:5000/oauth2")
                .build();
    }

    protected AccessToken accessToken;

    protected void givenAnInvalidAccessToken() {
        accessToken = new AccessToken.Builder(AbstractIntegrationTestBase.INVALID_ID).build();
    }

    protected String dateAsString(int year, int month, int date, int hourOfDay, int minute, int second, int millisecond) {
        Date completeDate = createDate(year, month, date, hourOfDay, minute, second, millisecond);
        return ISODateTimeFormat.dateTime().withZoneUTC().print(completeDate.getTime());
    }

    private Date createDate(int year, int month, int date, int hourOfDay, int minute, int second, int millisecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, millisecond);
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.set(year, month, date, hourOfDay, minute, second);
        return calendar.getTime();
    }
}
