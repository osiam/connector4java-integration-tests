package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.update.UpdateGroup;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed_bugfixing.xml")
@DatabaseTearDown(value = "/database_seed_bugfixing.xml", type = DatabaseOperation.DELETE_ALL)
public class BugFixingIT extends AbstractIntegrationTestBase {

    /*
     * This test will FAIL due to FOREIGN KEY CONSTRAINT on scim_group_scim_id (members_internal_id) REFERENCES scim_id(internal_id)
     * In hibernate the ON DELETE CASCADE is missing, and the DB is generated through the entities.
     *
     * In the real environment the sql script looks like that below and the test will pass.
     * ALTER TABLE ONLY scim_group_scim_id
     * ADD CONSTRAINT fk8d2c327bc347d0ba FOREIGN KEY (members_internal_id) REFERENCES scim_id(internal_id) ON DELETE CASCADE;
     */
    @Test
    public void user_should_be_deletable_as_member_of_a_group(){
        //given: user id, group id, add user to group
        String userIdForDeletion = "834b410a-943b-4c80-817a-4465aed037bc";
        String groupId = "69e1a5dc-89be-4343-976c-b5541af249f4";
        oConnector.updateGroup("69e1a5dc-89be-4343-976c-b5541af249f4",
                new UpdateGroup.Builder().addMember("834b410a-943b-4c80-817a-4465aed037bc").build(), accessToken);

        //when: user is deleted
        oConnector.deleteUser(userIdForDeletion, accessToken);

        //then: no error should occur and user is deleted
        try {
            oConnector.getUser(userIdForDeletion, accessToken);
        } catch (NoResultException e) {
            assert e.getMessage().equals("Resource 834b410a-943b-4c80-817a-4465aed037bc not found.");
        }

        Group group = oConnector.getGroup(groupId, accessToken);
        assert group != null;
        assert group.getId().equals("69e1a5dc-89be-4343-976c-b5541af249f4");
    }

    @Test
    public void user_should_not_be_deleted_if_the_group_he_belongs_to_is_deleted(){
        //given: group id for deletion, user id, add user to group
        String groupIdForDeletion = "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578";
        String userId = "618b398c-0110-43f2-95df-d1bc4e7d2b4a";
        oConnector.updateGroup("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578",
                new UpdateGroup.Builder().addMember("618b398c-0110-43f2-95df-d1bc4e7d2b4a").build(), accessToken);

        //when: group is deleted
        oConnector.deleteGroup(groupIdForDeletion, accessToken);

        //then: no error should occur and the user is still in the database
        try {
            oConnector.getGroup(groupIdForDeletion, accessToken);
        } catch (NoResultException e) {
            assert e.getMessage().equals("Resource d30a77eb-d7cf-4cd1-9fb3-cc640ef09578 not found.");
        }

        User user = oConnector.getUser(userId, accessToken);
        assert user != null;
        assert user.getId().equals("618b398c-0110-43f2-95df-d1bc4e7d2b4a");
    }

    @Test
    public void member_group_should_not_be_deleted_if_master_group_is_deleted(){
        //given: the group id for deletion, group member id, add group as member to group
        String groupIdForDeletion = "d30a77eb-d7cf-4cd1-9fb3-cc990ef09578";
        String memberId = "d30a77eb-d7cf-4cd1-9fb3-cc980ef09578";
        oConnector.updateGroup("d30a77eb-d7cf-4cd1-9fb3-cc990ef09578",
                new UpdateGroup.Builder().addMember("d30a77eb-d7cf-4cd1-9fb3-cc980ef09578").build(), accessToken);

        //when: the group is deleted
        oConnector.deleteGroup(groupIdForDeletion, accessToken);

        //then: the member group should be present in the database
        try {
            oConnector.getGroup(groupIdForDeletion, accessToken);
        } catch (NoResultException e) {
            assert e.getMessage().equals("Resource d30a77eb-d7cf-4cd1-9fb3-cc990ef09578 not found.");
        }

        Group memberGroup = oConnector.getGroup(memberId, accessToken);
        assert memberGroup != null;
        assert memberGroup.getId().equals("d30a77eb-d7cf-4cd1-9fb3-cc980ef09578");
    }
}