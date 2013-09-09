package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.Group_;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class EditGroupServiceIT extends AbstractIntegrationTestBase{

    private UUID VALID_UUID = null;
    private UUID ID_EXISTING_GROUP = UUID.fromString("69e1a5dc-89be-4343-976c-b5541af249f4");
    private UUID NEW_UUID = UUID.randomUUID();
    private String NAME_EXISTING_GROUP = "test_group01";
    private Group NEW_GROUP;
    private Group RETURN_GROUP;
    private Group DB_GROUP;
    private static final String IRRELEVANT = "irrelevant";
    private Query QUERY;

    @Test  (expected = ConflictException.class)
    public void create_group_with_no_username_raises_exception(){
        initializeGroupWithNoUserName();
        createGroup();
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_group_with_existing_displayName_raises_exception(){
        initializeGroupWithExistingDisplayName();
        createGroup();
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_empty_group_raises_exception(){
        initializeGroupWithEmptyDisplayName();
        createGroup();
        fail("Exception excpected");
    }

    @Test
    public void create_simple_Group(){
        initializeSimpleGroup();
        createGroup();
        returnGroupHasValidId();
        loadGroup(UUID.fromString(RETURN_GROUP.getId()));
        returnAndDbGroupHaveSameDislplayName();
    }

    @Test
    public void create_group_with_existing_uuid(){
        initializeSimpleGroupWithID(ID_EXISTING_GROUP.toString());
        createGroup();
        loadGroup(ID_EXISTING_GROUP);
        existingGroupDislpayNameHasNotChanged();
    }

    @Test
    public void given_uuid_to_new_group_has_changed_after_saving()
    {
        initializeSimpleGroupWithID(NEW_UUID.toString());
        createGroup();
        assertNotSame(NEW_UUID.toString(), RETURN_GROUP.getId());
    }

    @Test
    public void created_group_can_be_found(){
        initialQueryToSearchGroup();
        loadSingleGroupByQuery();
        assertNull(DB_GROUP);

        initializeSimpleGroup();
        createGroup();
        loadSingleGroupByQuery();
        assertNotNull(DB_GROUP);
        assertNotSame(IRRELEVANT, DB_GROUP.getDisplayName());
    }

    @Test
    public void uuid_return_group_same_as_new_loaded_uuid(){
        initializeSimpleGroupWithID(NEW_UUID.toString());
        createGroup();
        initialQueryToSearchGroup();
        loadSingleGroupByQuery();
        assertNotNull(DB_GROUP);
        assertEquals(RETURN_GROUP.getId(), DB_GROUP.getId());
    }
    
    @Test
    public void group_is_deleted() throws Exception {
    	given_a_test_group_UUID();
    	whenGroupIsDeleted();
    	thenGroupIsRemoveFromServer();
    }
    
    @Test (expected = NoResultException.class)
    public void user_is_not_deleted() throws Exception {
    	givenAValidUserUUIDForDeletion();
        whenUserIsDeleted();
        fail();
    }

    @Test (expected = NoResultException.class)
    public void delete_group_two_times() throws Exception {
    	given_a_test_group_UUID();
    	whenGroupIsDeleted();
    	thenGroupIsRemoveFromServer();
    	whenGroupIsDeleted();
    	fail();
    }

    @Test
    public void delete_group_with_members(){
        UUID uuidGroup01 = UUID.fromString("69e1a5dc-89be-4343-976c-b5541af249f4");
        oConnector.getGroup(uuidGroup01, accessToken);
        //group could be found
        oConnector.deleteGroup(uuidGroup01, accessToken);
        try{
            oConnector.getGroup(uuidGroup01, accessToken);
            fail("Exception excpected");
        }catch (NoResultException e){}
    }
    
    private void given_a_test_group_UUID() {
    	VALID_UUID = UUID.fromString(VALID_GROUP_UUID);
    }
    private void whenGroupIsDeleted() {
        oConnector.deleteGroup(VALID_UUID, accessToken);
    }
    private void thenGroupIsRemoveFromServer() {
    	try {
            oConnector.getGroup(VALID_UUID, accessToken);
    	} catch(NoResultException e) {
    		return;
    	} catch(Exception e) {
    		fail(Arrays.toString(e.getStackTrace()));
    	}
    	fail();
    }
    
    private void givenAValidUserUUIDForDeletion() throws Exception {
        VALID_UUID = UUID.fromString(DELETE_USER_UUID);
    }
    
    private void whenUserIsDeleted() {
        oConnector.deleteGroup(VALID_UUID, accessToken);
    }

    private void initializeGroupWithNoUserName(){
        NEW_GROUP = new Group.Builder().build();
    }

    private void initializeGroupWithEmptyDisplayName(){
        NEW_GROUP = new Group.Builder().setDisplayName("").build();
    }

    private void initializeSimpleGroup(){
        NEW_GROUP = new Group.Builder().setDisplayName(IRRELEVANT).build();
    }

    private void initializeSimpleGroupWithID(String id){
        NEW_GROUP = new Group.Builder().setDisplayName(IRRELEVANT).setId(id).build();
    }

    private void initializeGroupWithExistingDisplayName(){
        NEW_GROUP = new Group.Builder().setDisplayName(NAME_EXISTING_GROUP).build();
    }

    private void returnGroupHasValidId(){
        UUID.fromString(RETURN_GROUP.getId());
    }

    private void loadGroup(UUID id){
        DB_GROUP = oConnector.getGroup(id, accessToken);
    }

    private void loadSingleGroupByQuery(){
        QueryResult<Group> result = oConnector.searchGroups(QUERY, accessToken);
        if(result.getResources().size() == 0){
            DB_GROUP = null;
        }else if(result.getResources().size() == 1){
            DB_GROUP = result.getResources().get(0);
        }else{
            fail("No or one user should be found");
        }
    }

    private void existingGroupDislpayNameHasNotChanged(){
        assertEquals(NAME_EXISTING_GROUP, DB_GROUP.getDisplayName());
    }

    private void returnAndDbGroupHaveSameDislplayName(){
        assertEquals(NEW_GROUP.getDisplayName(), DB_GROUP.getDisplayName());
    }

    private void createGroup(){
        RETURN_GROUP = oConnector.createGroup(NEW_GROUP, accessToken);
    }

    private void initialQueryToSearchGroup(){
        QUERY = new Query.Builder(Group.class).setFilter(new Query.Filter(Group.class, Group_.displayName.equalTo(IRRELEVANT))).build();
    }
}
