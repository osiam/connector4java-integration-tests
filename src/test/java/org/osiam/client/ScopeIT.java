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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ForbiddenException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.SCIMSearchResult;
import org.osiam.resources.scim.UpdateGroup;
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
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed_scope.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class ScopeIT {

    private static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    protected static final String AUTH_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-auth-server";
    protected static final String RESOURCE_ENDPOINT_ADDRESS = "http://localhost:8180/osiam-resource-server";
    private static final String CLIENT_ID = "example-client";
    private static final String CLIENT_SECRET = "secret";
    private OsiamConnector oConnector;
    private AccessToken accessToken;

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
        retrieveAccessToken(Scope.DELETE);
        retrieveUser();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void getting_group_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        retrieveGroup();
        fail("Exception expected");
    }
    
    @Test(expected = ForbiddenException.class)
    public void getting_all_users_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        getAllUsers();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void getting_all_groups_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        getAllGroups();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void creating_a_user_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        createUser();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void creating_a_group_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        createGroup();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void getting_current_user_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        getCurrentUser();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void updating_user_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        updateUser();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void updating_group_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        updateGroup();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void searching_for_user_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        searchForUsers();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void searching_for_group_in_DELETE_scope_raises_exception() {
        retrieveAccessToken(Scope.DELETE);
        searchForGroups();
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void deleting_user_in_GET_scope_raises_exception() {
        retrieveAccessToken(Scope.GET);
        oConnector.deleteUser(VALID_USER_ID, accessToken);
        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void deleting_group_in_GET_scope_raises_exception() {
        retrieveAccessToken(Scope.GET);
        oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
        fail("Exception expected");
    }

    @Test
    public void get_user_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(retrieveUser(), is(notNullValue()));
    }

    @Test
    public void get_group_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(retrieveGroup(), is(notNullValue()));
    }

    @Test
    public void get_all_users_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(getAllUsers(), is(notNullValue()));
    }

    @Test
    public void get_all_groups_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(getAllGroups(), is(notNullValue()));
    }

    @Test
    public void get_current_user_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(getCurrentUser(), is(notNullValue()));
    }

    @Test
    public void create_user_in_POST_scope_works() {
        retrieveAccessToken(Scope.POST);
        assertThat(createUser(), is(notNullValue()));
    }

    @Test
    public void create_group_in_POST_scope_works() {
        retrieveAccessToken(Scope.POST);
        assertThat(createGroup(), is(notNullValue()));
    }

    @Test
    public void update_user_in_PATCH_scope_works() {
        retrieveAccessToken(Scope.PATCH);
        assertThat(updateUser(), is(notNullValue()));
    }

    @Test
    public void update_group_in_PATCH_scope_works() {
        retrieveAccessToken(Scope.ALL);
        assertThat(updateGroup(), is(notNullValue()));
    }

    @Test
    public void search_for_users_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(searchForUsers(), is(notNullValue()));
    }

    @Test
    public void search_for_groups_in_GET_scope_works() {
        retrieveAccessToken(Scope.GET);
        assertThat(searchForGroups(), is(notNullValue()));
    }

    @Test
    @ExpectedDatabase(value = "/database_expected_scope_delete_user.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void delete_user_in_DELETE_scope_works() {
        retrieveAccessToken(Scope.DELETE);
        oConnector.deleteUser(VALID_USER_ID, accessToken);
    }

    @Test
    @ExpectedDatabase(value = "/database_expected_scope_delete_group.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void delete_group_in_DELETE_scope_works() {
        retrieveAccessToken(Scope.DELETE);
        oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
    }

    private User createUser() {
        User user = new User.Builder("userName").build();
        return oConnector.createUser(user, accessToken);
    }

    private Group createGroup() {
        Group group = new Group.Builder("displayName").build();
        return oConnector.createGroup(group, accessToken);
    }

    private void retrieveAccessToken(Scope... scopes) {
        accessToken = oConnector.retrieveAccessToken("marissa", "koala", scopes);
    }

    private User retrieveUser() {
        return oConnector.getUser(VALID_USER_ID, accessToken);
    }

    private Group retrieveGroup() {
        return oConnector.getGroup(VALID_GROUP_ID, accessToken);
    }

    private List<User> getAllUsers() {
        return oConnector.getAllUsers(accessToken);
    }

    private List<Group> getAllGroups() {
        return oConnector.getAllGroups(accessToken);
    }

    private User getCurrentUser() {
        return oConnector.getCurrentUser(accessToken);
    }

    private User updateUser() {
        UpdateUser updateUser = new UpdateUser.Builder().updateUserName("newUserName").updateActive(false).build();
        return oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
    }

    private Group updateGroup() {
        UpdateGroup updateGroup = new UpdateGroup.Builder().updateDisplayName("irrelevant").build();
        return oConnector.updateGroup(VALID_GROUP_ID, updateGroup, accessToken);
    }

    private SCIMSearchResult<User> searchForUsers() {
        Query query = new QueryBuilder().startIndex(1).build();
        return oConnector.searchUsers(query, accessToken);
    }

    private SCIMSearchResult<Group> searchForGroups() {
        Query query = new QueryBuilder().startIndex(1).build();
        return oConnector.searchGroups(query, accessToken);
    }

}
