package org.osiam.client;

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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/database_seed_group_membership.xml")
@DatabaseTearDown(value = "/database_seed_group_membership.xml", type = DatabaseOperation.DELETE_ALL)
public class GroupMembershipIT extends AbstractIntegrationTestBase {

    private static final String USER_UUID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String PARENT_GROUP_UUID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private static final String MEMBER_GROUP_UUID = "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578";

    @Test(expected = NoResultException.class)
    public void deleting_a_user_who_is_member_of_a_group_works() {
        oConnector.deleteUser(USER_UUID, accessToken);

        oConnector.getUser(USER_UUID, accessToken);

        fail("Exception expected");
    }

    @Test
    public void deleting_a_user_who_is_member_of_a_group_does_not_delete_the_parent_group() {
        oConnector.deleteUser(USER_UUID, accessToken);

        Group group = oConnector.getGroup(PARENT_GROUP_UUID, accessToken);
        assertThat(group, is(notNullValue()));
    }

    @Test(expected = NoResultException.class)
    public void deleting_a_group_which_is_member_of_a_group_works() {
        oConnector.deleteGroup(MEMBER_GROUP_UUID, accessToken);

        oConnector.getGroup(MEMBER_GROUP_UUID, accessToken);

        fail("Exception expected");
    }

    @Test
    public void deleting_a_group_which_is_member_of_a_group_does_not_delete_the_parent_group() {
        oConnector.deleteGroup(MEMBER_GROUP_UUID, accessToken);
        Group group = oConnector.getGroup(PARENT_GROUP_UUID, accessToken);

        assertThat(group, is(notNullValue()));
    }

    @Test
    public void deleting_a_parent_group_does_not_delete_its_members() {
        oConnector.deleteGroup(PARENT_GROUP_UUID, accessToken);

        User user = oConnector.getUser(USER_UUID, accessToken);
        Group group = oConnector.getGroup(MEMBER_GROUP_UUID, accessToken);

        assertThat(user, is(notNullValue()));
        assertThat(group, is(notNullValue()));
    }

    @Test
    /*
       Todo: Scim-Schema does not yet support getGroups() on a Group. As soon as it does this test should be extended
             to verify that the memberGroup is contained in exactly one group.
     */
    public void group_memberships_are_visible_in_member_and_parent() {
        Group parentGroup = oConnector.getGroup(PARENT_GROUP_UUID, accessToken);
        User user = oConnector.getUser(USER_UUID, accessToken);

        assertThat(parentGroup.getMembers(), hasSize(2));
        assertThat(user.getGroups(), hasSize(1));

    }

}