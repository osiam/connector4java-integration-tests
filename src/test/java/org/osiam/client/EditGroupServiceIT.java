package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.Group_;
import org.osiam.resources.scim.Group;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed.xml")
@DatabaseTearDown(value = "/database_seed.xml", type = DatabaseOperation.DELETE_ALL)
public class EditGroupServiceIT extends AbstractIntegrationTestBase{


    private static String ID_EXISTING_GROUP = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private static String NEW_ID = UUID.randomUUID().toString();
    private static String NAME_EXISTING_GROUP = "test_group01";
    private static final String IRRELEVANT = "irrelevant";
    private String validId = null;
    private Group newGroup;
    private Group returnGroup;
    private Group dbGroup;
    private Query query;

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
        loadGroup(returnGroup.getId());
        
        assertEquals(newGroup.getDisplayName(), dbGroup.getDisplayName());
    }

    @Test
    public void create_group_with_existing_id(){
        initializeSimpleGroupWithID(ID_EXISTING_GROUP.toString());
        createGroup();
        loadGroup(ID_EXISTING_GROUP);
        assertEquals(NAME_EXISTING_GROUP, dbGroup.getDisplayName());
    }

    @Test
    public void given_id_to_new_group_has_changed_after_saving()
    {
        initializeSimpleGroupWithID(NEW_ID.toString());
        createGroup();
        assertNotEquals(NEW_ID.toString(), returnGroup.getId());
    }

    @Test
    public void created_group_can_be_found(){
        initialQueryToSearchGroup();
        loadSingleGroupByQuery();
        assertNull(dbGroup);

        initializeSimpleGroup();
        createGroup();
        loadSingleGroupByQuery();
        assertNotNull(dbGroup);
        assertEquals(IRRELEVANT, dbGroup.getDisplayName());
    }

    @Test
    public void id_return_group_same_as_new_loaded_id(){
        initializeSimpleGroupWithID(NEW_ID.toString());
        createGroup();
        initialQueryToSearchGroup();
        loadSingleGroupByQuery();
        assertNotNull(dbGroup);
        assertEquals(returnGroup.getId(), dbGroup.getId());
    }

    @Test
    public void group_is_deleted() throws Exception {
    	given_a_test_group_ID();
    	whenGroupIsDeleted();
    	thenGroupIsRemoveFromServer();
    }

    @Test (expected = NoResultException.class)
    public void user_is_not_deleted() throws Exception {
    	givenAValidUserIDForDeletion();
        whenUserIsDeleted();
        fail();
    }

    @Test (expected = NoResultException.class)
    public void delete_group_two_times() throws Exception {
    	given_a_test_group_ID();
    	whenGroupIsDeleted();
    	thenGroupIsRemoveFromServer();
    	whenGroupIsDeleted();
    	fail();
    }

    @Test
    public void delete_group_with_members(){
        String idGroup01 = "69e1a5dc-89be-4343-976c-b5541af249f4";
        oConnector.getGroup(idGroup01, accessToken);
        //group could be found
        oConnector.deleteGroup(idGroup01, accessToken);
        try{
            oConnector.getGroup(idGroup01, accessToken);
            fail("Exception excpected");
        }catch (NoResultException e){}
    }

    private void given_a_test_group_ID() {
    	validId = VALID_GROUP_ID;
    }

    private void whenGroupIsDeleted() {
        oConnector.deleteGroup(validId, accessToken);
    }

    private void thenGroupIsRemoveFromServer() {
    	try {
            oConnector.getGroup(validId, accessToken);
    	} catch(NoResultException e) {
    		return;
    	} catch(Exception e) {
    		fail(Arrays.toString(e.getStackTrace()));
    	}
    	fail();
    }

    private void givenAValidUserIDForDeletion() throws Exception {
        validId = DELETE_USER_ID;
    }

    private void whenUserIsDeleted() {
        oConnector.deleteGroup(validId, accessToken);
    }

    private void initializeGroupWithNoUserName(){
        newGroup = new Group.Builder().build();
    }

    private void initializeGroupWithEmptyDisplayName(){
        newGroup = new Group.Builder().setDisplayName("").build();
    }

    private void initializeSimpleGroup(){
        newGroup = new Group.Builder().setDisplayName(IRRELEVANT).build();
    }

    private void initializeSimpleGroupWithID(String id){
        newGroup = new Group.Builder().setDisplayName(IRRELEVANT).setId(id).build();
    }

    private void initializeGroupWithExistingDisplayName(){
        newGroup = new Group.Builder().setDisplayName(NAME_EXISTING_GROUP).build();
    }

    private void returnGroupHasValidId(){
        assertTrue(returnGroup.getId().length() > 0);
    }

    private void loadGroup(String id){
        dbGroup = oConnector.getGroup(id, accessToken);
    }

    private void loadSingleGroupByQuery(){
        QueryResult<Group> result = oConnector.searchGroups(query, accessToken);
        if(result.getResources().size() == 0){
            dbGroup = null;
        }else if(result.getResources().size() == 1){
            dbGroup = result.getResources().get(0);
        }else{
            fail("No or one user should be found");
        }
    }

    private void createGroup(){
        returnGroup = oConnector.createGroup(newGroup, accessToken);
    }

    private void initialQueryToSearchGroup(){
        query = new Query.Builder(Group.class).setFilter(new Query.Filter(Group.class, Group_.displayName.equalTo(IRRELEVANT))).build();
    }
}
