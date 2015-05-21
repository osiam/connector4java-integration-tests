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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ForbiddenException;
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

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seed_me_scope.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class MeScopeIT {

    private static final String OWN_USER_ID = "cef9452e-00a9-4cec-a086-d171374ffbef";
    private static final String OTHER_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";

    private final OsiamConnector oConnector = new OsiamConnector.Builder()
            .setAuthServerEndpoint(AbstractIntegrationTestBase.AUTH_ENDPOINT_ADDRESS)
            .setResourceServerEndpoint(AbstractIntegrationTestBase.RESOURCE_ENDPOINT_ADDRESS)
            .setClientId("example-client")
            .setClientSecret("secret")
            .build();

    @Test
    public void can_get_own_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        User user = oConnector.getUser(OWN_USER_ID, accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_update_own_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateDisplayName("Marissa")
                .addEmail(email)
                .build();

        User user = oConnector.updateUser(OWN_USER_ID, updateUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Marissa")));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_replace_own_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        User originalUser = oConnector.getUser(OWN_USER_ID, accessToken);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        User replaceUser = new User.Builder(originalUser)
                .setDisplayName("Marissa")
                .addEmail(email)
                .build();

        User user = oConnector.replaceUser(OWN_USER_ID, replaceUser, accessToken);

        assertThat(user.getDisplayName(), is(equalTo("Marissa")));
        assertThat(user.getEmails().get(0).getValue(), is(equalTo("marrisa@example.com")));
        assertThat(user.getEmails().get(0).getType(), is(equalTo(Email.Type.HOME)));
    }

    @Test
    public void can_delete_own_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.deleteUser(OWN_USER_ID, accessToken);
    }

    @Test
    public void can_get_me_basic() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        BasicUser user = oConnector.getCurrentUserBasic(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test
    public void can_get_me() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        User user = oConnector.getCurrentUser(accessToken);

        assertThat(user.getUserName(), is(equalTo("marissa")));
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_all_users() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.getAllUsers(accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_own_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder().filter("userName eq \"marissa\"").build();

        oConnector.searchUsers(query, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_any_users() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder()
                .filter("meta.created gt \"2010-10-10T00:00:00.000\"")
                .build();

        oConnector.searchUsers(query, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_create_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        User user = new User.Builder("newUser").build();

        oConnector.createUser(user, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_other_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.getUser(OTHER_USER_ID, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_update_other_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        UpdateUser updateUser = new UpdateUser.Builder()
                .updateDisplayName("Marissa")
                .addEmail(email)
                .build();

        oConnector.updateUser(OTHER_USER_ID, updateUser, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_replace_other_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        User originalUser = oConnector.getUser(OWN_USER_ID, accessToken);
        Email email = new Email.Builder()
                .setValue("marrisa@example.com")
                .setType(Email.Type.HOME)
                .build();
        User replaceUser = new User.Builder(originalUser)
                .setDisplayName("Marissa")
                .addEmail(email)
                .build();

        oConnector.replaceUser(OTHER_USER_ID, replaceUser, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_delete_other_user() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.deleteUser(OTHER_USER_ID, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_group() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.getGroup(GROUP_ID, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_create_group() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group group = new Group.Builder("newGroup")
                .setMembers(Collections.singleton(memberRef))
                .build();

        oConnector.createGroup(group, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_update_group() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .addMember(OWN_USER_ID)
                .updateDisplayName("newDisplayName")
                .build();

        oConnector.updateGroup(GROUP_ID, updateGroup, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_replace_group() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        MemberRef memberRef = new MemberRef.Builder()
                .setValue(OWN_USER_ID)
                .setType(MemberRef.Type.USER)
                .build();
        Group group = new Group.Builder("test_group01")
                .setMembers(Collections.singleton(memberRef))
                .build();

        oConnector.replaceGroup(GROUP_ID, group, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_delete_group() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.deleteGroup(GROUP_ID, accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_get_all_groups() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);

        oConnector.getAllGroups(accessToken);

        fail("Exception expected");
    }

    @Test(expected = ForbiddenException.class)
    public void cannot_search_for_groups() {
        AccessToken accessToken = oConnector.retrieveAccessToken("marissa", "koala", Scope.ME);
        Query query = new QueryBuilder().filter("displayName eq \"test_group01\"").build();

        oConnector.searchGroups(query, accessToken);

        fail("Exception expected");
    }

}
