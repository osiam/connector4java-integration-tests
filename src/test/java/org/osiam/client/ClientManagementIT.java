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
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ClientAlreadyExistsException;
import org.osiam.client.exception.ClientNotFoundException;
import org.osiam.client.oauth.Client;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class ClientManagementIT extends AbstractIntegrationTestBase {

    @Before
    public void setup() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    @Test
    public void get_client_by_id() {
        final Client client = OSIAM_CONNECTOR.getClient("example-client", accessToken);

        assertThat(client.getId(), is("example-client"));
    }

    @Test
    public void get_clients() {
        final List<Client> clients = OSIAM_CONNECTOR.getClients(accessToken);

        assertThat(clients, hasSize(3));
        assertTrue(containsClient(clients, "example-client"));
        assertTrue(containsClient(clients, "short-living-client"));
        assertTrue(containsClient(clients, "auth-server"));
    }

    private boolean containsClient(List<Client> clients, String clientId) {
        for (Client client : clients) {
            if (client.getId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void create_client() {
        Client osiamClient = new Client.Builder("example-client-2", "secret-2")
                .accessTokenValiditySeconds(2342).refreshTokenValiditySeconds(2342)
                .redirectUri("http://localhost:5055/oauth2").scopes(Sets.newHashSet(Scope.ADMIN.getValue()))
                .grants(Sets.newHashSet(GrantType.REFRESH_TOKEN.name(), GrantType.CLIENT_CREDENTIALS.name(),
                        GrantType.AUTHORIZATION_CODE.name(), GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS.name()))
                .implicit(false)
                .validityInSeconds(1337)
                .build();

        final Client createdClient = OSIAM_CONNECTOR.createClient(osiamClient, accessToken);

        assertThat(createdClient.getId(), is("example-client-2"));
        assertThat(createdClient.getRedirectUri(), is("http://localhost:5055/oauth2"));
    }

    @Test(expected = ClientAlreadyExistsException.class)
    public void cant_create_client_with_already_existing_id() {
        OSIAM_CONNECTOR.createClient(new Client.Builder("example-client", "secret").build(), accessToken);
    }

    @Test(expected = ClientNotFoundException.class)
    public void delete_client() {
        OSIAM_CONNECTOR.deleteClient("short-living-client", accessToken);

        OSIAM_CONNECTOR.getClient("short-living-client", accessToken);
    }

    @Test
    public void delete_not_existing_client_raises_no_exception() {
        OSIAM_CONNECTOR.deleteClient("not-existing-client", accessToken);
    }

    @Test
    public void update_client() {
        Client client = new Client.Builder("example-client", "secret-2")
                .accessTokenValiditySeconds(1).refreshTokenValiditySeconds(1)
                .redirectUri("http://localhost:5055/oauth2").scopes(Sets.newHashSet(Scope.ME.getValue()))
                .grants(Sets.newHashSet(GrantType.REFRESH_TOKEN.name(), GrantType.CLIENT_CREDENTIALS.name(),
                        GrantType.AUTHORIZATION_CODE.name()))
                .implicit(true)
                .validityInSeconds(1)
                .build();

        Client updatedClient = OSIAM_CONNECTOR.updateClient("example-client", client, accessToken);

        assertEquals(client, updatedClient);
    }
}
