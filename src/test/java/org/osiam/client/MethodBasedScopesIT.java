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
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ForbiddenException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seeds/MethodBasedScopesIT/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class MethodBasedScopesIT {

    private static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    private static final String CLIENT_ID = "example-client";
    private static final String CLIENT_SECRET = "secret";
    private OsiamConnector oConnector;

    @Before
    public void setUp() throws Exception {
        oConnector = new OsiamConnector.Builder()
                .setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();
    }

    @Test(expected = ForbiddenException.class)
    public void getting_user_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.getUser(VALID_USER_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void getting_group_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.getGroup(VALID_GROUP_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void getting_all_users_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.getAllUsers(accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void getting_all_groups_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.getAllGroups(accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void creating_a_user_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        User user = new User.Builder("userName").build();
        oConnector.createUser(user, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void creating_a_group_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        Group group = new Group.Builder("displayName").build();
        oConnector.createGroup(group, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void getting_current_user_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.getMe(accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void updating_user_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateUserName("newUserName")
                .updateActive(false)
                .build();

        oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void updating_group_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .updateDisplayName("irrelevant")
                .build();
        oConnector.updateGroup(VALID_GROUP_ID, updateGroup, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void searching_for_user_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        Query query = new QueryBuilder()
                .startIndex(1)
                .build();
        oConnector.searchUsers(query, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void searching_for_group_in_DELETE_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        Query query = new QueryBuilder()
                .startIndex(1)
                .build();
        oConnector.searchGroups(query, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void deleting_user_in_GET_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        oConnector.deleteUser(VALID_USER_ID, accessToken);
    }

    @Test(expected = ForbiddenException.class)
    public void deleting_group_in_GET_scope_raises_exception() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
    }

    @Test
    public void get_user_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        User user = oConnector.getUser(VALID_USER_ID, accessToken);
        assertThat(user, is(notNullValue()));
    }

    @Test
    public void get_group_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        Group group = oConnector.getGroup(VALID_GROUP_ID, accessToken);
        assertThat(group, is(notNullValue()));
    }

    @Test
    public void get_all_users_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        List<User> allUsers = oConnector.getAllUsers(accessToken);
        assertThat(allUsers, is(notNullValue()));
    }

    @Test
    public void get_all_groups_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        List<Group> allGroups = oConnector.getAllGroups(accessToken);
        assertThat(allGroups, is(notNullValue()));
    }

    @Test
    public void get_current_user_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        User user = oConnector.getMe(accessToken);
        assertThat(user, is(notNullValue()));
    }

    @Test
    public void create_user_in_POST_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.POST);
        User user = new User.Builder("userName").build();
        User createdUser = oConnector.createUser(user, accessToken);
        assertThat(createdUser, is(notNullValue()));
    }

    @Test
    public void create_group_in_POST_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.POST);
        Group group = new Group.Builder("displayName").build();
        Group createdGroup = oConnector.createGroup(group, accessToken);
        assertThat(createdGroup, is(notNullValue()));
    }

    @Test
    public void update_user_in_PATCH_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.PATCH, Scope.POST);
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateUserName("newUserName")
                .updateActive(false)
                .build();
        User updatedUser = oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
        assertThat(updatedUser, is(notNullValue()));
    }

    @Test
    public void update_group_in_PATCH_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ALL);
        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .updateDisplayName("irrelevant")
                .build();
        Group updatedGroup = oConnector.updateGroup(VALID_GROUP_ID, updateGroup, accessToken);
        assertThat(updatedGroup, is(notNullValue()));
    }

    @Test
    public void search_for_users_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        Query query = new QueryBuilder()
                .startIndex(1)
                .build();
        SCIMSearchResult<User> users = oConnector.searchUsers(query, accessToken);
        assertThat(users, is(notNullValue()));
    }

    @Test
    public void search_for_groups_in_GET_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);
        Query query = new QueryBuilder()
                .startIndex(1)
                .build();
        SCIMSearchResult<Group> groups = oConnector.searchGroups(query, accessToken);
        assertThat(groups, is(notNullValue()));
    }

    @Test(expected = NoResultException.class)
    @ExpectedDatabase(value = "/database_seeds/MethodBasedScopesIT/database_expected_delete_user.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void delete_user_in_DELETE_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE, Scope.POST);
        oConnector.deleteUser(VALID_USER_ID, accessToken);
        oConnector.getUser(VALID_USER_ID, oConnector.retrieveAccessToken("marissa", "koala", Scope.GET));
    }

    @Test(expected = NoResultException.class)
    @ExpectedDatabase(value = "/database_seeds/MethodBasedScopesIT/database_expected_delete_group.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void delete_group_in_DELETE_scope_works() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.DELETE);
        oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
        oConnector.getGroup(VALID_GROUP_ID, oConnector.retrieveAccessToken("marissa", "koala", Scope.GET));
    }

    @Test
    public void different_scopes_different_token() {
        oConnector = new OsiamConnector.Builder()
                .setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS)
                .setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS)
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();
        AccessToken accessTokenWithPost = oConnector.retrieveAccessToken("marissa", "koala", Scope.POST);
        AccessToken accessTokenWithGet = oConnector.retrieveAccessToken("marissa", "koala", Scope.GET);

        assertFalse(accessTokenWithGet.getToken().equals(accessTokenWithPost.getToken()));
    }

}
