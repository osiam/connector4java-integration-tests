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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed_admin_scope.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class AdminScopeIT extends AbstractIntegrationTestBase {

    private static final String OWN_USER_ID = "cef9452e-00a9-4cec-a086-d171374ffbef";
    private static final String OTHER_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";

    @Test
    public void can_access_ServiceProviderConfigs_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(RESOURCE_ENDPOINT_ADDRESS)
                .path("ServiceProviderConfigs")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity("irrelevant", MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_access_root() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(RESOURCE_ENDPOINT_ADDRESS)
                .path("/")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(501)));
    }

    @Test
    public void can_access_root_with_post() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(RESOURCE_ENDPOINT_ADDRESS)
                .path(".search")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity("irrelevant", MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        assertThat(response.getStatus(), is(equalTo(501)));
    }

    @Test
    public void can_access_metrics_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(RESOURCE_ENDPOINT_ADDRESS)
                .path("Metrics")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_access_extensions_endpoint() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(RESOURCE_ENDPOINT_ADDRESS)
                .path("osiam").path("extension-definition")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_get_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        User user = OSIAM_CONNECTOR.getUser(OWN_USER_ID, accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_update_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
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
        assertThat(user.isActive(), is(equalTo(false)));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_replace_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
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
        assertThat(user.isActive(), is(equalTo(false)));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_delete_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.deleteUser(OWN_USER_ID, accessToken);
    }

    @Test
    public void can_get_me_basic() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        BasicUser user = OSIAM_CONNECTOR.getCurrentUserBasic(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_get_me() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        User user = OSIAM_CONNECTOR.getCurrentUser(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_get_all_users() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        List<User> users = OSIAM_CONNECTOR.getAllUsers(accessToken);

        assertThat(users, hasSize(2));
    }

    @Test
    public void can_search_for_own_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        Query query = new QueryBuilder().filter("userName eq \"marissa\"").build();

        SCIMSearchResult<User> users = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(users.getTotalResults(), is(equalTo(1L)));
        User user = users.getResources().get(0);
        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_search_for_any_users() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        Query query = new QueryBuilder()
                .filter("meta.created gt \"2010-10-10T00:00:00.000\"")
                .build();

        SCIMSearchResult<User> users = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(users.getTotalResults(), is(equalTo(2L)));
    }

    @Test
    public void can_create_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        User userToCreate = new User.Builder("newUser").build();

        User user = OSIAM_CONNECTOR.createUser(userToCreate, accessToken);

        assertThat(user.getUserName(), is(equalTo("newUser")));
    }

    @Test
    public void can_get_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        User user = OSIAM_CONNECTOR.getUser(OTHER_USER_ID, accessToken);

        assertThat(user.getUserName(), is(equalTo("bjensen")));
    }

    @Test
    public void can_update_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        Email email = new Email.Builder()
                .setValue("barbara@example.com")
                .setType(Email.Type.HOME)
                .build();
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateDisplayName("Barbara")
                .updateActive(false)
                .addEmail(email)
                .build();

        User user = OSIAM_CONNECTOR.updateUser(OTHER_USER_ID, updateUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Barbara")));
        assertThat(user.isActive(), is(equalTo(false)));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("barbara@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_replace_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        User originalUser = OSIAM_CONNECTOR.getUser(OTHER_USER_ID, accessToken);
        Email email = new Email.Builder()
                .setValue("barbara@example.com")
                .setType(Email.Type.HOME)
                .build();
        User replaceUser = new User.Builder(originalUser)
                .setDisplayName("Barbara")
                .setActive(false)
                .addEmail(email)
                .build();

        User user = OSIAM_CONNECTOR.replaceUser(OTHER_USER_ID, replaceUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Barbara")));
        assertThat(user.isActive(), is(equalTo(false)));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("barbara@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_delete_other_user() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.deleteUser(OTHER_USER_ID, accessToken);
    }

    @Test
    public void can_get_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Group group = OSIAM_CONNECTOR.getGroup(GROUP_ID, accessToken);

        assertThat(group.getDisplayName(), is(equalTo("test_group01")));
    }

    @Test
    public void can_create_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group groupToCreate = new Group.Builder("newGroup")
                .setMembers(Collections.singleton(memberRef))
                .build();

        Group group = OSIAM_CONNECTOR.createGroup(groupToCreate, accessToken);

        assertThat(group.getDisplayName(), is(equalTo("newGroup")));
    }

    @Test
    public void can_update_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .addMember(OWN_USER_ID)
                .updateDisplayName("newDisplayName")
                .build();

        Group group = OSIAM_CONNECTOR.updateGroup(GROUP_ID, updateGroup, accessToken);

        assertThat(group.getDisplayName(), is(equalTo("newDisplayName")));
    }

    @Test
    public void can_replace_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group groupToReplace = new Group.Builder("test_group01")
                .setMembers(Collections.singleton(memberRef))
                .build();

        Group group = OSIAM_CONNECTOR.replaceGroup(GROUP_ID, groupToReplace, accessToken);

        assertThat(group.getMembers(), hasSize(2));
    }

    @Test
    public void can_delete_group() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.deleteGroup(GROUP_ID, accessToken);
    }

    @Test
    public void can_get_all_groups() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        List<Group> groups = OSIAM_CONNECTOR.getAllGroups(accessToken);

        assertThat(groups, hasSize(1));
    }

    @Test
    public void can_search_for_groups() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        Query query = new QueryBuilder().filter("displayName eq \"test_group01\"").build();

        SCIMSearchResult<Group> groups = OSIAM_CONNECTOR.searchGroups(query, accessToken);

        assertThat(groups.getTotalResults(), is(equalTo(1L)));
    }

    @Test(expected = UnauthorizedException.class)
    public void can_revoke_access_token() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.revokeAccessToken(accessToken);

        OSIAM_CONNECTOR.validateAccessToken(accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void can_revoke_all_access_tokens() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.revokeAllAccessTokens(OWN_USER_ID, accessToken);

        OSIAM_CONNECTOR.validateAccessToken(accessToken);
    }

    @Test
    public void can_validate_access_token() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        accessToken = OSIAM_CONNECTOR.validateAccessToken(accessToken);

        assertThat(accessToken.getUserId(), is(equalTo(OWN_USER_ID)));
        assertThat(accessToken.getUserName(), is(equalTo("marissa")));
    }

    @Test(expected = UnauthorizedException.class)
    public void can_revoke_all_access_tokens_of_another_user() {
        AccessToken accessTokenOfOtherUser = OSIAM_CONNECTOR.retrieveAccessToken("bjensen", "koala", Scope.ADMIN);
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        OSIAM_CONNECTOR.revokeAllAccessTokens(OTHER_USER_ID, accessToken);

        OSIAM_CONNECTOR.validateAccessToken(accessTokenOfOtherUser);
    }

    @Test
    public void can_retrieve_a_client() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(AUTH_ENDPOINT_ADDRESS)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_retrieve_clients() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(AUTH_ENDPOINT_ADDRESS)
                .path("Client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .get();

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_create_a_client() {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        String clientAsJsonString = "{\"id\":\"example-client-2\",\"accessTokenValiditySeconds\":2342,\"refreshTokenValiditySeconds\":2342,"
                + "\"redirectUri\":\"http://localhost:5055/oauth2\",\"client_secret\":\"secret-2\","
                + "\"scope\":[\"ADMIN\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\",\"password\"],"
                + "\"implicit\":false,\"validityInSeconds\":1337}";

        Response response = CLIENT.target(AUTH_ENDPOINT_ADDRESS)
                .path("Client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .post(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(equalTo(201)));
    }

    @Test
    public void can_delete_a_client() throws IOException {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);

        Response response = CLIENT.target(AUTH_ENDPOINT_ADDRESS)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .delete();

        assertThat(response.getStatus(), is(equalTo(200)));
    }

    @Test
    public void can_update_a_client() throws JSONException {
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
        String clientAsJsonString = "{\"id\":\"example-client\",\"accessTokenValiditySeconds\":1,\"refreshTokenValiditySeconds\":1,"
                + "\"redirectUri\":\"http://newhost:5000/oauth2\",\"client_secret\":\"secret\","
                + "\"scope\":[\"ADMIN\"],"
                + "\"grants\":[\"refresh_token\",\"client_credentials\",\"authorization_code\"],"
                + "\"implicit\":true,\"validityInSeconds\":1}";

        Response response = CLIENT.target(AUTH_ENDPOINT_ADDRESS)
                .path("Client").path("example-client")
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken.getToken())
                .put(Entity.entity(clientAsJsonString, MediaType.APPLICATION_JSON));

        assertThat(response.getStatus(), is(equalTo(200)));
    }

}
