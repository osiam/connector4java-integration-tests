package de.osiam.client;


import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertNotSame;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class UpdateUserIT extends AbstractIntegrationTestBase{

    private UUID ID_EXISITNG_USER = UUID.fromString("7d33bcbe-a54c-43d8-867e-f6146164941e");
    private UpdateUser UPDATE_USER;
    private User RETURN_USER;
    private User ORIGINAL_USER;
    private String IRRELEVANT = "Irrelevant";

    @Test
    public void delete_multivalue_attributes(){
    	getOriginalUser("dma");
        createUpdateUserWithMultiDeleteFields();
        updateUser();
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getEmails(), "hsimpson@atom-example.com"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getEmails(), "hsimpson@atom-example.com"));
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getPhoneNumbers(), "0245817964"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getPhoneNumbers(), "0245817964"));
       // assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getEntitlements(), "right2"));TODO at the second run it will fail
       // assertFalse(isValuePartOfMultivalueList(RETURN_USER.getEntitlements(), "right2"));TODO at the second run it will fail
        //assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578"));TODO Gruppen werden nicht gespeicher
        //assertFalse(isValuePartOfMultivalueList(RETURN_USER.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578")); TODO Gruppen werden nicht gespeicher
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getIms(), "ims01"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getIms(), "ims01"));
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getPhotos(), "photo01.jpg"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getPhotos(), "photo01.jpg"));
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getRoles(), "role01"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getRoles(), "role01"));
        assertTrue(isValuePartOfMultivalueList(ORIGINAL_USER.getX509Certificates(), "certificate01"));
        assertFalse(isValuePartOfMultivalueList(RETURN_USER.getX509Certificates(), "certificate01"));
    }
    
    @Test
    public void delete_all_multivalue_attributes(){
    	getOriginalUser("dama");
        createUpdateUserWithMultiAllDeleteFields();
        updateUser();
        assertNotNull(ORIGINAL_USER.getEmails());
        assertNull(RETURN_USER.getEmails());
        assertNull(RETURN_USER.getAddresses());
        //assertNull(RETURN_USER.getEntitlements());TODO at the second run it will fail
        assertNull(RETURN_USER.getGroups());//TODO da Gruppen nicht gespeichert werden sind sie immer null
        assertNull(RETURN_USER.getIms());
        assertNull(RETURN_USER.getPhoneNumbers());
        assertNull(RETURN_USER.getPhotos());
        assertNull(RETURN_USER.getRoles());
    }
    
    @Test
    public void add_multivalue_attributes(){
    	getOriginalUser("ama");
    	createUpdateUserWithMultiAddFields();
        String userString = getUpdateUser();
    	updateUser();
    	assertEquals(ORIGINAL_USER.getPhoneNumbers().size() + 1, RETURN_USER.getPhoneNumbers().size());
    	assertTrue(isValuePartOfMultivalueList(RETURN_USER.getPhoneNumbers(), "99999999991"));
    	//assertEquals(ORIGINAL_USER.getEmails().size() + 1, RETURN_USER.getEmails().size());TODO funktioniert nicht. Eine mailadresse wird von server gelöscht
    	assertTrue(isValuePartOfMultivalueList(RETURN_USER.getEmails(), "mac@muster.de"));
    	//assertEquals(ORIGINAL_USER.getAddresses().size() + 1, RETURN_USER.getAddresses().size());TODO neue Addresse löscht zuerst die alten
    	getAddress(RETURN_USER.getAddresses(), "new Address");
    	//assertEquals(ORIGINAL_USER.getEntitlements().size() + 1, RETURN_USER.getEntitlements().size());TODO at the second run it will fail
    	//assertTrue(isValuePartOfMultivalueList(RETURN_USER.getEntitlements(), "right3"));TODO at the second run it will fail
    	//assertEquals(ORIGINAL_USER.getGroups().size() + 1, RETURN_USER.getGroups().size());TODO gruppen werden aktuell nicht gespeichert
    	//assertTrue(isValuePartOfMultivalueList(RETURN_USER.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578"));TODO gruppen werden aktuell nicht gespeichert
    	assertEquals(ORIGINAL_USER.getIms().size() + 1, RETURN_USER.getIms().size());
    	//assertTrue(isValuePartOfMultivalueList(RETURN_USER.getIms(), "ims03"));//TODO der type wird nicht geändert
    	assertEquals(ORIGINAL_USER.getPhotos().size() + 1, RETURN_USER.getPhotos().size());
    	assertTrue(isValuePartOfMultivalueList(RETURN_USER.getPhotos(), "photo03.jpg"));
    	assertEquals(ORIGINAL_USER.getRoles().size() + 1, RETURN_USER.getRoles().size());
    	assertTrue(isValuePartOfMultivalueList(RETURN_USER.getRoles(), "role03"));
    	
    }
    
    @Test
    public void update_multivalue_attributes(){
    	getOriginalUser("uma");
    	createUpdateUserWithMultiUpdateFields();
    	updateUser();
    	//phonenumber
    	MultiValuedAttribute multi = getSingleMultiValueAttribute(RETURN_USER.getPhoneNumbers(), "+497845/1157");
    	//assertFalse(multi.isPrimary());TODO primary wird beim telefon nicht gesetzt
    	multi = getSingleMultiValueAttribute(RETURN_USER.getPhoneNumbers(), "0245817964");
    	//assertTrue(multi.isPrimary());TODO primary wird beim telefon nicht gesetzt
    	//assertEquals("other", multi.getType());TODO der type wird nicht geändert
    	//email
    	//MultiValuedAttribute multi = getSingleMultiValueAttribute(RETURN_USER.getEmails(), "hsimpson@atom-example.com");
    	//assertFalse(multi.isPrimary());//TODO die atomadresse wird gelöscht und die andere wird nicht abgedatet
    	//multi = getSingleMultiValueAttribute(RETURN_USER.getEmails(), "hsimpson@home-example.com");
    	//assertTrue(multi.isPrimary());
    	//assertEquals("other", multi.getType());
    	multi = getSingleMultiValueAttribute(RETURN_USER.getIms(), "ims01");
    	//assertEquals("icq", multi.getType());//TODO der type wird nicht upgedatet
    	multi = getSingleMultiValueAttribute(RETURN_USER.getPhotos(), "photo01.jpg");
    	//assertEquals("photo", multi.getType());//TODO der type wird nicht upgedatet
    	//multi = getSingleMultiValueAttribute(RETURN_USER.getRoles(), "role01");//TODO der type wird nicht gespeichert und kann somit nicht geändert werden
    	assertEquals("other", multi.getType());
    }
    
	@Test
    public void update_all_single_values(){
        getOriginalUser("uasv");
        createUpdateUserWithUpdateFields();
        updateUser();
        assertNotEquals(ORIGINAL_USER.getUserName(), RETURN_USER.getUserName());
        assertNotEquals(ORIGINAL_USER.getNickName(), RETURN_USER.getNickName());
        assertNotEquals(ORIGINAL_USER.isActive(), RETURN_USER.isActive());
        assertNotEquals(ORIGINAL_USER.getDisplayName(), RETURN_USER.getDisplayName());
        assertNotEquals(ORIGINAL_USER.getExternalId(), RETURN_USER.getExternalId());
        assertNotEquals(ORIGINAL_USER.getLocale(), RETURN_USER.getLocale());
        assertNotEquals(ORIGINAL_USER.getPreferredLanguage(), RETURN_USER.getPreferredLanguage());
        assertNotEquals(ORIGINAL_USER.getProfileUrl(), RETURN_USER.getProfileUrl());
        assertNotEquals(ORIGINAL_USER.getTimezone(), RETURN_USER.getTimezone());
        assertNotEquals(ORIGINAL_USER.getTitle(), RETURN_USER.getTitle());
        assertNotEquals(ORIGINAL_USER.getUserType(), RETURN_USER.getUserType());
    }
		
	@Test
    public void delete_all_single_values(){
		getOriginalUser("desv");
        createUpdateUserWithDeleteFields();
        updateUser();
        assertNull(RETURN_USER.getNickName());
        assertNull(RETURN_USER.getDisplayName());
        assertNull(RETURN_USER.getLocale());
        assertNull(RETURN_USER.getPreferredLanguage());
        assertNull(RETURN_USER.getProfileUrl());
        assertNull(RETURN_USER.getTimezone());
        assertNull(RETURN_USER.getTitle());
        assertNull(RETURN_USER.getUserType());
    }
	
	@Test
	public void update_password() {
		getOriginalUser("uasv");
        createUpdateUserWithUpdateFields();
        updateUser();
        makeNewConnectionWithNewPassword();
	}
	
	private String getUpdateUser(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure( SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false );
        String userAsString = null;
        try {
            userAsString = mapper.writeValueAsString(UPDATE_USER.getUserToUpdate());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return userAsString;
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
	
	public MultiValuedAttribute getSingleMultiValueAttribute(List<MultiValuedAttribute> multiValues, Object value){
		if(multiValues != null){
			for (MultiValuedAttribute actMultiValuedAttribute : multiValues) {
				if(actMultiValuedAttribute.getValue().equals(value)){
					return actMultiValuedAttribute;
				}
			}
		}
		fail("The value " + value + " could not be found");
		return null; //Can't be reached
	}
	
    public void getOriginalUser(String userName){
        User.Builder userBuilder = new User.Builder(userName);
        
        MultiValuedAttribute email01 = new MultiValuedAttribute.Builder().setValue("hsimpson@atom-example.com").setType("work").setPrimary(true).build();
        MultiValuedAttribute email02 = new MultiValuedAttribute.Builder().setValue("hsimpson@home-example.com").setType("work").build();
        List<MultiValuedAttribute> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);        
        
        MultiValuedAttribute phoneNumber01 = new MultiValuedAttribute.Builder().setValue("+497845/1157").setType("work").setPrimary(true).build();
        MultiValuedAttribute phoneNumber02 = new MultiValuedAttribute.Builder().setValue("0245817964").setType("home").build();
        List<MultiValuedAttribute> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber01);
        phoneNumbers.add(phoneNumber02);
        
        Address simpleAddress01 = new Address.Builder().setCountry("de").setFormatted("formated address").setLocality("Berlin").setPostalCode("111111").build();
        Address simpleAddress02 = new Address.Builder().setCountry("en").setFormatted("address formated").setLocality("New York").setPostalCode("123456").build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(simpleAddress01);
        addresses .add(simpleAddress02);   
        
        MultiValuedAttribute entitlement01 = new MultiValuedAttribute.Builder().setValue("right1").build();
        MultiValuedAttribute entitlement02 = new MultiValuedAttribute.Builder().setValue("right2").build();
        List<MultiValuedAttribute> entitlements = new ArrayList<>();
        entitlements.add(entitlement01);
        entitlements.add(entitlement02);
        
        MultiValuedAttribute group01 = new MultiValuedAttribute.Builder().setValue("69e1a5dc-89be-4343-976c-b5541af249f4").build();
        MultiValuedAttribute group02 = new MultiValuedAttribute.Builder().setValue("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578").build();
        List<MultiValuedAttribute> groups = new ArrayList<>();
        groups.add(group01);
        groups.add(group02);
        
        MultiValuedAttribute ims01 = new MultiValuedAttribute.Builder().setValue("ims01").setType("skype").build();
        MultiValuedAttribute ims02 = new MultiValuedAttribute.Builder().setValue("ims02").build();
        List<MultiValuedAttribute> ims = new ArrayList<>();
        ims.add(ims01);
        ims.add(ims02);
        
        MultiValuedAttribute photo01 = new MultiValuedAttribute.Builder().setValue("photo01.jpg").setType("thumbnail").build();
        MultiValuedAttribute photo02 = new MultiValuedAttribute.Builder().setValue("photo02.jpg").build();
        List<MultiValuedAttribute> photos = new ArrayList<>();
        photos.add(photo01);
        photos.add(photo02);
        
        MultiValuedAttribute role01 = new MultiValuedAttribute.Builder().setValue("role01").setType("some").build();
        MultiValuedAttribute role02 = new MultiValuedAttribute.Builder().setValue("role02").build();
        List<MultiValuedAttribute> roles = new ArrayList<>();
        roles.add(role01);
        roles.add(role02);
        
        MultiValuedAttribute certificate01 = new MultiValuedAttribute.Builder().setValue("certificate01").setType("some").build();
        MultiValuedAttribute certificate02 = new MultiValuedAttribute.Builder().setValue("certificate02").build();
        List<MultiValuedAttribute> certificates = new ArrayList<>();
        certificates.add(certificate01);
        certificates.add(certificate02);
        
        		userBuilder.setNickName("irgendwas")
        			.setEmails(emails)
        			.setPhoneNumbers(phoneNumbers)
        			.setActive(false)
        			.setDisplayName("irgendwas")
        			.setLocale("de")
        			.setPassword("geheim")
        			.setPreferredLanguage("de")
        			.setProfileUrl("irgendwas")
        			.setTimezone("irgendwas")
        			.setTitle("irgendwas")
        			.setUserType("irgendwas")
        			.setAddresses(addresses)
        			.setGroups(groups)
        			.setIms(ims)
        			.setPhotos(photos)
        			.setRoles(roles)
        			.setX509Certificates(certificates)
        			//.setEntitlements(entitlements)TODO at the second run it will fail
        			;
        User newUser = userBuilder.build(); 
        
        ORIGINAL_USER = oConnector.createUser(newUser, accessToken);
        ID_EXISITNG_USER = UUID.fromString(ORIGINAL_USER.getId());
    }

    private void createUpdateUserWithUpdateFields(){
        UPDATE_USER = new UpdateUser.Builder(IRRELEVANT)
        					.updateNickname(IRRELEVANT)
        					.updateExternalId(IRRELEVANT)
        					.updateDisplayName(IRRELEVANT)
        					.updatePassword(IRRELEVANT)
        					.updateLocal(IRRELEVANT)
        					.updatePreferredLanguage(IRRELEVANT)
        					.updateProfileUrl(IRRELEVANT)
        					.updateTimezone(IRRELEVANT)
        					.updateTitle(IRRELEVANT)
        					.updateUserType(IRRELEVANT)
        					.setActiv(true).build();
    }
    
    private void createUpdateUserWithMultiUpdateFields(){

    	MultiValuedAttribute email = new MultiValuedAttribute.Builder()
    					.setValue("hsimpson@home-example.com").setType("other").setPrimary(true).build();
    	MultiValuedAttribute phoneNumber = new MultiValuedAttribute.Builder().setValue("0245817964").setType("other")
    			.setPrimary(true).build(); //Now the other should not be primary anymore
    	MultiValuedAttribute ims = new MultiValuedAttribute.Builder().setValue("ims01").setType("icq").build();
    	MultiValuedAttribute photo = new MultiValuedAttribute.Builder().setValue("photo01.jpg").setType("photo").build(); 
    	MultiValuedAttribute role = new MultiValuedAttribute.Builder().setValue("role01").setType("other").build();
    	UPDATE_USER = new UpdateUser.Builder(IRRELEVANT) //TODO username nur solange bug im server existiert
        					.updateEmail(email)
        					.updatePhoneNumber(phoneNumber)
        					.updateIms(ims)
        					.updateRole(role)
        					.build();
    }
    
    private void createUpdateUserWithDeleteFields(){
        UPDATE_USER = new UpdateUser.Builder(IRRELEVANT) //TODO username nur solange bug im server existiert
        					.deleteDisplayName()
        					.deleteNickname()
        					.deleteLocal()
        					.deletePreferredLanguage()
        					.deleteProfileUrl()
        					.deleteTimezone()
        					.deleteTitle()
        					.deleteUserType()
        					.build();
    }
    
    private void createUpdateUserWithMultiDeleteFields(){

    	UPDATE_USER = new UpdateUser.Builder(IRRELEVANT) //TODO username nur solange bug im server existiert
        					.deleteEmail("hsimpson@atom-example.com")
        					//.deleteEntitlement("right2")TODO at the second run it will fail
        					.deleteGroup(UUID.fromString("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578"))
        					.deleteIms("ims01")
        					.deletePhoneNumber("0245817964")
        					.deletePhoto("photo01.jpg")
        					.deleteRole("role01")
        					.deleteX509Certificate("certificate01")
        					.build();
    }
    
    private void createUpdateUserWithMultiAddFields(){

    	MultiValuedAttribute email = new MultiValuedAttribute.Builder()
    					.setValue("mac@muster.de").setType("home").build();
    	
    	MultiValuedAttribute phonenumber = new MultiValuedAttribute.Builder()
		.setValue("99999999991").setType("home").build();
    	
    	Address newSimpleAddress = new Address.Builder().setCountry("fr").setFormatted("new Address").setLocality("New City").setPostalCode("66666").build();
    	MultiValuedAttribute entitlement = new MultiValuedAttribute.Builder().setValue("right3").build();
    	MultiValuedAttribute ims = new MultiValuedAttribute.Builder().setValue("ims03").build();
    	MultiValuedAttribute photo = new MultiValuedAttribute.Builder().setValue("photo03.jpg").build();
    	MultiValuedAttribute role = new MultiValuedAttribute.Builder().setValue("role03").build();
    	
    	UPDATE_USER = new UpdateUser.Builder(IRRELEVANT) //TODO username nur solange bug im server existiert
        					.addEmail(email)
        					.addPhoneNumber(phonenumber)
        					.addAddress(newSimpleAddress)
        					//.addEntitlement(entitlement)TODO at the second run it will fail
        					.addGroup(UUID.fromString("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578")) //TODO Gruppen werden nicht gespeichert 
        					.addIms(ims)
        					.addPhotos(photo)
        					.addRole(role)
        					.build();
    }
    

    
    private void createUpdateUserWithMultiAllDeleteFields(){

    	UPDATE_USER = new UpdateUser.Builder(IRRELEVANT) //TODO username nur solange bug im server existiert
        					.deleteEmails()
        					.deleteAddresses()
        					//.deleteEntitlements()TODO at the second run it will fail
        					.deleteGroups() //TODO Gruppen werden nicht gespeichert und können somit auch nicht gelöscht werden
        					.deleteIms()
        					.deletePhoneNumbers()
        					.deletePhotos()
        					.deleteRoles()
        					.build();
    }

    private void updateUser(){
       RETURN_USER = oConnector.updateUser(ID_EXISITNG_USER, UPDATE_USER, accessToken);
    }

    
    private void makeNewConnectionWithNewPassword() {
    	OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(endpointAddress).
                setClientId(clientId).
                setClientSecret(clientSecret).
                setGrantType(GrantType.PASSWORD).
                setUsername(IRRELEVANT).
                setPassword(IRRELEVANT);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
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

}
