package de.osiam.client;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.update.UpdateGroup;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MultiValuedAttribute;
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
@DatabaseSetup("/database_seed.xml")
public class UpdateGroupIT extends AbstractIntegrationTestBase{

    private static UUID idExistingGroup = UUID.fromString("7d33bcbe-a54c-43d8-867e-f6146164941e");
    private static UUID ID_USER_BTHOMSON = UUID.fromString("618b398c-0110-43f2-95df-d1bc4e7d2b4a");
    private static UUID ID_USER_CMILLER = UUID.fromString("ac3bacc9-915d-4bab-9145-9eb600d5e5bf");
    private static UUID ID_USER_HSIMPSON = UUID.fromString("7d33bcbe-a54c-43d8-867e-f6146164941e");
    private static UUID ID_GROUP_01 = UUID.fromString("69e1a5dc-89be-4343-976c-b5541af249f4");
    private static UUID ID_GROUP_02 = UUID.fromString("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578");
    private static String IRRELEVANT = "Irrelevant";
    private UpdateGroup updateGroup;
    private Group returnGroup;
    private Group originalGroup;

	@Test
    public void update_all_single_values(){
        getOriginalGroup();
        createUpdateUserWithUpdateFields();
        updateGroup();
        assertNotEquals(originalGroup.getDisplayName(), returnGroup.getDisplayName());
        assertNotEquals(originalGroup.getExternalId(), returnGroup.getExternalId());
    }
	
	@Test
    public void delete_all_single_values(){
		getOriginalGroup();
        createUpdateGroupWithDeleteFields();
        updateGroup();
        assertNull(returnGroup.getExternalId());
    }
	
	@Test
    public void delete_all_members(){
		getOriginalGroup();
        createUpdateGroupWithDeletedMembers();
        updateGroup();
        assertNotNull(originalGroup.getMembers());
        assertNull(returnGroup.getMembers());
    }
	
	@Test (expected = ConflictException.class)
    public void try_update_with_wrong_id_raises_exception(){
        getOriginalGroup();
        createUpdateUserWithUpdateFields();
        idExistingGroup = UUID.randomUUID();
        updateGroup();
    }
	
	@Test //(expected = ConflictException.class)
    public void try_update_with_user_id_raises_exception(){
        getOriginalGroup();
        createUpdateUserWithUpdateFields();
        idExistingGroup = ID_USER_HSIMPSON;
        updateGroup();
    }
	
	@Test (expected = ConflictException.class)
    public void set_display_name_to_empty_string_to_raise_exception(){
        getOriginalGroup();
        createUpdateUserWithEmptyDisplayName();
        updateGroup();
        fail("exception expected");
    }
	
	@Test (expected = ConflictException.class)
    public void add_new_mebers(){
		getOriginalGroup();
		createUpdateGroupWithAddingMembers();
        updateGroup();
        assertEquals(originalGroup.getMembers().size() + 2, returnGroup.getMembers().size()); 
        MultiValuedAttribute value = getSingleMultiValueAttribute(returnGroup.getMembers(), ID_USER_HSIMPSON);
        assertNotNull(value);
        value = getSingleMultiValueAttribute(returnGroup.getMembers(), ID_GROUP_02);
        assertNotNull(value);
    }
	
	@Test
    public void add_invalid_mebers(){
		getOriginalGroup();
		createUpdateGroupWithAddingInvalidMembers();
        updateGroup();
    }
	
	@Test
    public void delete_one_user_and_one_group_member(){
		getOriginalGroup();
		createUpdateGroupWithDeleteOneMembers();
        updateGroup();
        assertEquals(originalGroup.getMembers().size() - 2, returnGroup.getMembers().size()); 
        MultiValuedAttribute value = getSingleMultiValueAttribute(returnGroup.getMembers(), ID_USER_HSIMPSON);
        assertNull(value);
        value = getSingleMultiValueAttribute(returnGroup.getMembers(), ID_GROUP_01);
        assertNull(value);
    }

	@Test
	@Ignore //the new member is not been added at the moment
	public void delete_all_members_and_add_one_member(){
		getOriginalGroup();
		createUpdateGroupWithDeleteAllMembersAndAddingOneMember();
		updateGroup();
		assertNotNull(returnGroup.getMembers());
		assertEquals(1,returnGroup.getMembers().size());
		MultiValuedAttribute value = getSingleMultiValueAttribute(returnGroup.getMembers(), ID_USER_HSIMPSON);
        assertNotNull(value);
	}
	
	public boolean isValuePartOfMultivalueList(List<MultiValuedAttribute> list, String value){
		if(list != null){
			for (MultiValuedAttribute actAttribute : list) {
				if(actAttribute.getValue().equals(value)){
					return true;
				}
			}
		}
		return false;
	}
	
	public MultiValuedAttribute getSingleMultiValueAttribute(Set<MultiValuedAttribute> multiValues, Object value){
		if(multiValues != null){
			for (MultiValuedAttribute actMultiValuedAttribute : multiValues) {
				if(actMultiValuedAttribute.getValue().toString().equals(value.toString())){
					return actMultiValuedAttribute;
				}
			}
		}
		return null;
	}
	
    private void createUpdateGroupWithDeleteFields(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.deleteExternalId()
        					.build();
    }
    
    private void createUpdateGroupWithDeletedMembers(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.deleteMembers()
        					.build();
    }
    
    private void createUpdateGroupWithAddingMembers(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.addMember(ID_USER_HSIMPSON)
        					.addMember(ID_GROUP_02)
        					.build();
    }
    
    private void createUpdateGroupWithAddingInvalidMembers(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.addMember(UUID.randomUUID())
        					.build();
    }
    
    private void createUpdateGroupWithDeleteOneMembers(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.deleteMember(ID_USER_CMILLER)
        					.deleteMember(ID_GROUP_01)
        					.build();
    }
    
    private void createUpdateGroupWithDeleteAllMembersAndAddingOneMember(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)//TODO needs to be set bug in server
        					.deleteMembers()
        					.addMember(ID_USER_HSIMPSON)
        					.build();
    }
	
    public void getOriginalGroup(){
        Group.Builder userBuilder = new Group.Builder();
        
        MultiValuedAttribute member01 = new MultiValuedAttribute.Builder().setValue(ID_USER_BTHOMSON).setType("hallo").build();
        MultiValuedAttribute member02 = new MultiValuedAttribute.Builder().setValue(ID_USER_CMILLER).build();
        MultiValuedAttribute member03 = new MultiValuedAttribute.Builder().setValue(ID_GROUP_01).build();
        Set<MultiValuedAttribute> members = new HashSet<>();
        members.add(member01);
        members.add(member02);
        members.add(member03); 

        		userBuilder
        			.setMembers(members)
        			.setDisplayName("irgendwas")
        			.setExternalId("irgendwas")
        			;
        Group newGroup = userBuilder.build(); 
        
        originalGroup = oConnector.createGroup(newGroup, accessToken);
        idExistingGroup = UUID.fromString(originalGroup.getId());
    }

    private void createUpdateUserWithUpdateFields(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName(IRRELEVANT)
        					.updateExternalId(IRRELEVANT)
        					.build();
    }
    
    private void createUpdateUserWithEmptyDisplayName(){
        updateGroup = new UpdateGroup.Builder()
        					.updateDisplayName("")
        					.build();
    }
        
    private void updateGroup(){
       returnGroup = oConnector.updateGroup(idExistingGroup, updateGroup, accessToken);
    }

    
	public Address getAddress(List<Address> addresses, String formated){
		if(addresses != null){
			for (Address actAddress : addresses) {
				if(actAddress.getFormatted().equals(formated)){
					return actAddress;
				}
			}
		}
		fail("The address with the formated part of " + formated + " could not be found");
		return null; //Can't be reached
	}

	private String getUpdateGroup(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false );
        String userAsString = null;
        try {
            userAsString = mapper.writeValueAsString(updateGroup.getScimConformUpdateGroup());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return userAsString;
	}
}
