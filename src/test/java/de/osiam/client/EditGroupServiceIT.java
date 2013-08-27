package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamGroupService;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.Group_;
import org.osiam.client.query.metamodel.User_;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.*;

import static junit.framework.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class EditGroupServiceIT extends AbstractIntegrationTestBase{

    private OsiamGroupService service;

    @Before
    public void setUp() throws Exception {
        service = new OsiamGroupService.Builder(endpointAddress).build();
    }

    @Test  (expected = ConflictException.class)
    public void create_group_with_no_username_raises_exception(){
        Group newGroup = new Group.Builder().build();
        service.createGroup(newGroup, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_group_with_exisitng_displayName_raises_exception(){
        Group newGroup = new Group.Builder().setDisplayName("test_group01").build();
        service.createGroup(newGroup, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_empty_group_raises_exception(){
        Group newGroup = new Group.Builder().setDisplayName("").build();
        service.createGroup(newGroup, accessToken);
        fail("Exception excpected");
    }

    @Test
    public void create_simple_Group(){
        Group newGroup = new Group.Builder().setDisplayName("crg").build();
        Group savedGroup = service.createGroup(newGroup, accessToken);
        assertTrue(savedGroup.getId().length() > 0);
        Group dbGroup = service.getGroupByUUID(UUID.fromString(savedGroup.getId()), accessToken);
        assertEquals(newGroup.getDisplayName(), dbGroup.getDisplayName());
    }

    @Test
    public void create_group_with_existing_uuid(){
        String group01Id = "69e1a5dc-89be-4343-976c-b5541af249f4";
        Group newGroup = new Group.Builder().setDisplayName("cgweu").setId(group01Id).build();
        service.createGroup(newGroup, accessToken);
        Group dbGroup = service.getGroupByUUID(UUID.fromString(group01Id), accessToken);

        assertEquals("test_group01", dbGroup.getDisplayName());
    }

    @Test
    public void given_uuid_to_new_group_has_changed_after_saving()
    {
        String groupId = "1d83bcbe-a54c-43d8-867e-f6146164941e";
        Group newGroup = new Group.Builder().setDisplayName("gutnuhcas").setId(groupId).build();
        Group savedGroup = service.createGroup(newGroup, accessToken);

        assertNotSame(groupId, savedGroup.getId());
    }

    @Test
    public void created_group_can_be_found(){
        String displayName = "cgcbf";

        Query query = new Query.Builder(Group.class).filter(new Query.Filter(Group.class).startsWith(Group_.displayName.equalTo(displayName))).build();
        QueryResult<Group> result = service.searchGroups(query, accessToken);
        assertEquals(0, result.getResources().size());

        Group newGroup = new Group.Builder().setDisplayName(displayName).build();
        service.createGroup(newGroup, accessToken);

        result = service.searchGroups(query, accessToken);
        assertEquals(1, result.getResources().size());
        Group dbGroup = result.getResources().get(0);
        assertNotSame(displayName, dbGroup.getDisplayName());
    }

    @Test
    public void uuid_return_group_same_as_new_loaded_uuid(){
        String userId = "1d83bcbe-a54c-43d8-867e-f6146164941e";
        String displayName = "urgsanlu";
        Group newGroup = new Group.Builder().setDisplayName(displayName).setId(userId).build();
        Group savedGroup = service.createGroup(newGroup, accessToken);
        Query query = new Query.Builder(Group.class).filter(new Query.Filter(Group.class).startsWith(Group_.displayName.equalTo(displayName))).build();
        QueryResult<Group> result = service.searchGroups(query, accessToken);

        assertEquals(1, result.getResources().size());
        Group dbGroup = result.getResources().get(0);
        assertEquals(savedGroup.getId(), dbGroup.getId());
    }

}
