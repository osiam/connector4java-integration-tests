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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.NoResultException;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.User;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/database_seed_group_membership.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class GroupMembershipIT extends AbstractIntegrationTestBase {

    private static final String USER_UUID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String PARENT_GROUP_UUID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private static final String MEMBER_GROUP_UUID = "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578";

    @Before
    public void setup() {
        retrieveAccessTokenForMarissa();
    }

    @Test(expected = NoResultException.class)
    public void deleting_a_user_who_is_member_of_a_group_works() {
        OSIAM_CONNECTOR.deleteUser(USER_UUID, accessToken);

        OSIAM_CONNECTOR.getUser(USER_UUID, accessToken);

        fail("Exception expected");
    }

    @Test
    public void deleting_a_user_who_is_member_of_a_group_does_not_delete_the_parent_group() {
        OSIAM_CONNECTOR.deleteUser(USER_UUID, accessToken);

        Group group = OSIAM_CONNECTOR.getGroup(PARENT_GROUP_UUID, accessToken);
        assertThat(group, is(notNullValue()));
    }

    @Test(expected = NoResultException.class)
    public void deleting_a_group_which_is_member_of_a_group_works() {
        OSIAM_CONNECTOR.deleteGroup(MEMBER_GROUP_UUID, accessToken);

        OSIAM_CONNECTOR.getGroup(MEMBER_GROUP_UUID, accessToken);

        fail("Exception expected");
    }

    @Test
    public void deleting_a_group_which_is_member_of_a_group_does_not_delete_the_parent_group() {
        OSIAM_CONNECTOR.deleteGroup(MEMBER_GROUP_UUID, accessToken);
        Group group = OSIAM_CONNECTOR.getGroup(PARENT_GROUP_UUID, accessToken);

        assertThat(group, is(notNullValue()));
    }

    @Test
    public void deleting_a_parent_group_does_not_delete_its_members() {
        OSIAM_CONNECTOR.deleteGroup(PARENT_GROUP_UUID, accessToken);

        User user = OSIAM_CONNECTOR.getUser(USER_UUID, accessToken);
        Group group = OSIAM_CONNECTOR.getGroup(MEMBER_GROUP_UUID, accessToken);

        assertThat(user, is(notNullValue()));
        assertThat(group, is(notNullValue()));
    }

    @Test
    /*
     * Todo: Scim-Schema does not yet support getGroups() on a Group. As soon as it does this test should be extended to
     * verify that the memberGroup is contained in exactly one group.
     */
    public void group_memberships_are_visible_in_member_and_parent() {
        Group parentGroup = OSIAM_CONNECTOR.getGroup(PARENT_GROUP_UUID, accessToken);
        User user = OSIAM_CONNECTOR.getUser(USER_UUID, accessToken);

        assertThat(parentGroup.getMembers(), hasSize(2));
        assertThat(user.getGroups(), hasSize(1));

    }
}