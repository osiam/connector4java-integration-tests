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
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ForbiddenException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.client.user.BasicUser;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed_me_scope.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class MeScopeIT extends AbstractIntegrationTestBase {

    private static final String OWN_USER_ID = "cef9452e-00a9-4cec-a086-d171374ffbef";
    private static final String OTHER_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";

    @Test
    public void can_get_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        User user = OSIAM_CONNECTOR.getUser(OWN_USER_ID, accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_update_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateDisplayName("Marissa")
                .updateActive(false)
                .addEmail(email)
                .build();

        User user = OSIAM_CONNECTOR.updateUser(OWN_USER_ID, updateUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Marissa")));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_replace_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        User originalUser = OSIAM_CONNECTOR.getUser(OWN_USER_ID, accessToken);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        User replaceUser = new User.Builder(originalUser)
                .setDisplayName("Marissa")
                .setActive(false)
                .addEmail(email)
                .build();

        User user = OSIAM_CONNECTOR.replaceUser(OWN_USER_ID, replaceUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Marissa")));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_delete_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.deleteUser(OWN_USER_ID, accessToken);
    }

    @Test
    public void can_get_me_basic() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        BasicUser user = OSIAM_CONNECTOR.getCurrentUserBasic(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_get_me() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        User user = OSIAM_CONNECTOR.getCurrentUser(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_access_ServiceProviderConfig_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("ServiceProviderConfig")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity("irrelevant", MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_all_users() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.getAllUsers(accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder().filter("userName eq \"marissa\"").build();

        OSIAM_CONNECTOR.searchUsers(query, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_any_users() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder()
                .filter("meta.created gt \"2010-10-10T00:00:00.000\"")
                .build();

        OSIAM_CONNECTOR.searchUsers(query, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_create_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        User user = new User.Builder("newUser").build();

        OSIAM_CONNECTOR.createUser(user, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.getUser(OTHER_USER_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_update_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateDisplayName("Marissa")
                .addEmail(email)
                .build();

        OSIAM_CONNECTOR.updateUser(OTHER_USER_ID, updateUser, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_replace_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        User originalUser = OSIAM_CONNECTOR.getUser(OWN_USER_ID, accessToken);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        User replaceUser = new User.Builder(originalUser)
                .setDisplayName("Marissa")
                .addEmail(email)
                .build();

        OSIAM_CONNECTOR.replaceUser(OTHER_USER_ID, replaceUser, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_delete_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.deleteUser(OTHER_USER_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.getGroup(GROUP_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_create_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group group = new Group.Builder("newGroup")
                .setMembers(Collections.singleton(memberRef))
                .build();

        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_update_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .addMember(OWN_USER_ID)
                .updateDisplayName("newDisplayName")
                .build();

        OSIAM_CONNECTOR.updateGroup(GROUP_ID, updateGroup, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_replace_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group group = new Group.Builder("test_group01")
                .setMembers(Collections.singleton(memberRef))
                .build();

        OSIAM_CONNECTOR.replaceGroup(GROUP_ID, group, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_delete_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.deleteGroup(GROUP_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_all_groups() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.getAllGroups(accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_groups() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder().filter("displayName eq \"test_group01\"").build();

        OSIAM_CONNECTOR.searchGroups(query, accessToken);
    }

    @Test
    public void cannot_access_root() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("/")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_access_root_with_post() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path(".search")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity("irrelevant", MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_access_metrics_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Metrics")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_access_extensions_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("osiam").path("extension-definition")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test(expected = UnauthorizedException.class)
    public void can_revoke_access_token() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.revokeAccessToken(accessToken);

        OSIAM_CONNECTOR.validateAccessToken(accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void can_revoke_all_access_tokens() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.revokeAllAccessTokens(OWN_USER_ID, accessToken);

        OSIAM_CONNECTOR.validateAccessToken(accessToken);
    }

    @Test
    public void can_validate_access_token() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        accessToken = OSIAM_CONNECTOR.validateAccessToken(accessToken);

        assertThat(accessToken.getUserId(), is(equalTo(OWN_USER_ID)));
        assertThat(accessToken.getUserName(), is(equalTo("marissa")));
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_revoke_all_access_tokens_of_another_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        OSIAM_CONNECTOR.revokeAllAccessTokens(OTHER_USER_ID, accessToken);

    }

    @Test
    public void cannot_retrieve_a_client() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_retrieve_clients() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_create_a_client() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        String clientAsJsonString = "{\"id\":\"example-client-2\",\"accessTokenValiditySeconds\":2342,\"refreshTokenValiditySeconds\":2342,"
                + "\"redirectUri\":\"http://localhost:5055/oauth2\",\"client_secret\":\"secret-2\","
                + "\"scope\":[\"ADMIN\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\",\"password\"],"
                + "\"implicit\":false,\"validityInSeconds\":1337}";

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_delete_a_client() throws IOException {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .delete();

        assertThat(response.getStatus(), is(equalTo(403)));
    }

    @Test
    public void cannot_update_a_client() throws JSONException {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ME);
        String clientAsJsonString = "{\"id\":\"example-client\",\"accessTokenValiditySeconds\":1,\"refreshTokenValiditySeconds\":1,"
                + "\"redirectUri\":\"http://newhost:5000/oauth2\",\"client_secret\":\"secret\","
                + "\"scope\":[\"ADMIN\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\"],"
                + "\"implicit\":true,\"validityInSeconds\":1}";

        Response response = CLIENT.target(OSIAM_ENDPOINT)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .put(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(equalTo(403)));
    }
}
