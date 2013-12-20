package org.osiam.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class UpdateUserIT extends AbstractIntegrationTestBase {

    private String idExistingUser = "7d33bcbe-a54c-43d8-867e-f6146164941e";
    private UpdateUser updateUser;
    private User originalUser;
    private User returnUser;
    private User databaseUser;
    private static String IRRELEVANT = "Irrelevant";

    @Test
    public void delete_multivalue_attributes() {
        getOriginalUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();

        assertFalse(isValuePartOfMultivalueList(returnUser.getEmails(), "hsimpson@atom-example.com"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getPhoneNumbers(), "0245817964"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getIms(), "ims01"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getPhotos(), "photo01.jpg"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getRoles(), "role01"));
        assertFalse(isValuePartOfAddressList(returnUser.getAddresses(), "formated address 01"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getX509Certificates(), "certificate01"));
    }

    @Test
    @Ignore("write a private equals method to compare both users for value-equality")
    public void compare_returned_user_with_database_user() {
        getOriginalUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();

        assertTrue(returnUser.equals(databaseUser));
    }

    @Test
    public void REGT_015_delete_multivalue_attributes_twice() {
        getOriginalUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();
        updateUser();

        assertTrue(isValuePartOfMultivalueList(originalUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfMultivalueList(databaseUser.getEntitlements(), "right2"));
        assertTrue(isValuePartOfMultivalueList(originalUser.getX509Certificates(), "certificate01"));
        assertFalse(isValuePartOfMultivalueList(returnUser.getX509Certificates(), "certificate01"));
        assertFalse(isValuePartOfMultivalueList(databaseUser.getX509Certificates(), "certificate01"));
    }

    @Test
    public void delete_all_multivalue_attributes() {
        getOriginalUser("dama");
        createUpdateUserWithMultiAllDeleteFields();

        updateUser();

        assertThat(returnUser.getEmails(), is(empty()));
        assertThat(returnUser.getAddresses(), is(empty()));
        assertThat(returnUser.getEntitlements(), is(empty()));
        assertThat(returnUser.getIms(), is(empty()));
        assertThat(returnUser.getPhoneNumbers(), is(empty()));
        assertThat(returnUser.getPhotos(), is(empty()));
        assertThat(returnUser.getRoles(), is(empty()));
        assertThat(returnUser.getX509Certificates(), is(empty()));
    }

    @Test
    public void add_multivalue_attributes() {
        getOriginalUser("ama");
        createUpdateUserWithMultiAddFields();

        updateUser();

        assertEquals(originalUser.getPhoneNumbers().size() + 1, returnUser.getPhoneNumbers().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getPhoneNumbers(), "99999999991"));
        assertEquals(originalUser.getEmails().size() + 1, returnUser.getEmails().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getEmails(), "mac@muster.de"));
        assertEquals(originalUser.getAddresses().size() + 1, returnUser.getAddresses().size());
        getAddress(returnUser.getAddresses(), "new Address");
        assertEquals(originalUser.getEntitlements().size() + 1, returnUser.getEntitlements().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getEntitlements(), "right3"));
        assertEquals(originalUser.getIms().size() + 1, returnUser.getIms().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getIms(), "ims03"));
        assertEquals(originalUser.getPhotos().size() + 1, returnUser.getPhotos().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getPhotos(), "photo03.jpg"));
        assertEquals(originalUser.getRoles().size() + 1, returnUser.getRoles().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getRoles(), "role03"));
        assertEquals(originalUser.getX509Certificates().size() + 1, returnUser.getX509Certificates().size());
        assertTrue(isValuePartOfMultivalueList(returnUser.getX509Certificates(), "certificate03"));
    }

    @Test
    public void update_all_single_values() {
        getOriginalUser("uasv");
        createUpdateUserWithUpdateFields();

        updateUser();

        assertEquals("UserName", returnUser.getUserName());
        assertEquals("NickName", returnUser.getNickName());
        assertNotEquals(originalUser.isActive(), returnUser.isActive());
        assertEquals("DisplayName", returnUser.getDisplayName());
        assertEquals("ExternalId", returnUser.getExternalId());
        assertEquals("Locale", returnUser.getLocale());
        assertEquals("PreferredLanguage", returnUser.getPreferredLanguage());
        assertEquals("ProfileUrl", returnUser.getProfileUrl());
        assertEquals("Timezone", returnUser.getTimezone());
        assertEquals("Title", returnUser.getTitle());
        assertEquals("UserType", returnUser.getUserType());
        assertEquals("FamilyName", returnUser.getName().getFamilyName());
        assertEquals("ExternalId", returnUser.getExternalId());
    }

    @Test
    public void delete_all_single_values() {
        getOriginalUser("desv");
        createUpdateUserWithDeleteFields();

        updateUser();

        assertNull(returnUser.getNickName());
        assertNull(returnUser.getDisplayName());
        assertNull(returnUser.getLocale());
        assertNull(returnUser.getPreferredLanguage());
        assertNull(returnUser.getProfileUrl());
        assertNull(returnUser.getTimezone());
        assertNull(returnUser.getTitle());
        assertNull(returnUser.getUserType());
        assertNull(returnUser.getName());
        assertNull(returnUser.getExternalId());
    }

    @Test
    public void update_password() {
        getOriginalUser("uasv");
        createUpdateUserWithUpdateFields();

        updateUser();

        makeNewConnectionWithNewPassword();
    }

    @Test
    public void change_one_field_and_other_attributes_are_the_same() {
        getOriginalUser("cnaoaats");
        createUpdateUserWithJustOtherNickname();

        updateUser();

        assertNotEquals(originalUser.getNickName(), returnUser.getNickName());
        assertEquals(originalUser.isActive(), returnUser.isActive());
        assertEquals(originalUser.getDisplayName(), returnUser.getDisplayName());
        assertEquals(originalUser.getExternalId(), returnUser.getExternalId());
        assertEquals(originalUser.getLocale(), returnUser.getLocale());
        assertEquals(originalUser.getPreferredLanguage(), returnUser.getPreferredLanguage());
        assertEquals(originalUser.getProfileUrl(), returnUser.getProfileUrl());
        assertEquals(originalUser.getTimezone(), returnUser.getTimezone());
        assertEquals(originalUser.getTitle(), returnUser.getTitle());
        assertEquals(originalUser.getUserType(), returnUser.getUserType());
        assertEquals(originalUser.getName().getFamilyName(), returnUser.getName().getFamilyName());
    }

    @Test
    public void username_is_set_no_empty_string_is_thrown_probably() {
        getOriginalUser("ietiuuitp");
        createUpdateUserWithEmptyUserName();

        updateUser();

        assertThat(returnUser.getUserName(), is(equalTo(originalUser.getUserName())));
    }

    @Test
    public void update_attributes_doesnt_change_the_password() {
        getOriginalUser("uadctp");
        createUpdateUserWithUpdateFieldsWithoutPassword();

        updateUser();

        assertThat(retrieveNewAccessToken(), is(notNullValue()));
    }

    @Test (expected = ConflictException.class)
    public void updating_the_username_to_existing_username_raises_exception() {
        createUpdateUserWithNewUserName("marissa");

        updateUser();
    }

    @Test
    public void adding_new_primary_email_address_sets_the_other_to_non_primary(){
        getOriginalUser(IRRELEVANT);
        createUpdateUserWithNewPrimaryEmailAddress();

        updateUser();

        assertThatOnlyNewEmailAddressIsPrimary();
    }

    private void assertThatOnlyNewEmailAddressIsPrimary(){
        for(MultiValuedAttribute email : returnUser.getEmails()){
            if(email.getValue().equals("hsimpson02@atom-example.com")){
                assertThat(email.isPrimary(), is(true));
            }else{
                assertThat(email.isPrimary(), is(false));
            }
        }
    }

    private boolean isValuePartOfMultivalueList(List<MultiValuedAttribute> list, String value) {
        if (list != null) {
            for (MultiValuedAttribute actAttribute : list) {
                if (actAttribute.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfAddressList(List<Address> list, String formated) {
        if (list != null) {
            for (Address actAttribute : list) {
                if (actAttribute.getFormatted().equals(formated)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getOriginalUser(String userName) {
        User.Builder userBuilder = new User.Builder(userName);

        MultiValuedAttribute email01 = new MultiValuedAttribute.Builder().setValue("hsimpson@atom-example.com")
                .setType("work").setPrimary(true).build();
        MultiValuedAttribute email02 = new MultiValuedAttribute.Builder().setValue("hsimpson@home-example.com")
                .setType("work").build();
        List<MultiValuedAttribute> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);

        MultiValuedAttribute phoneNumber01 = new MultiValuedAttribute.Builder().setValue("+497845/1157")
                .setType("work").setPrimary(true).build();
        MultiValuedAttribute phoneNumber02 = new MultiValuedAttribute.Builder().setValue("0245817964").setType("work")
                .build();
        List<MultiValuedAttribute> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber01);
        phoneNumbers.add(phoneNumber02);

        Address simpleAddress01 = new Address.Builder().setCountry("de").setFormatted("formated address 01")
                .setLocality("Berlin").setPostalCode("111111").build();
        Address simpleAddress02 = new Address.Builder().setCountry("en").setFormatted("address formated 02")
                .setLocality("New York").setPostalCode("123456").build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(simpleAddress01);
        addresses.add(simpleAddress02);

        MultiValuedAttribute entitlement01 = new MultiValuedAttribute.Builder().setValue("right1").build();
        MultiValuedAttribute entitlement02 = new MultiValuedAttribute.Builder().setValue("right2").build();
        List<MultiValuedAttribute> entitlements = new ArrayList<>();
        entitlements.add(entitlement01);
        entitlements.add(entitlement02);

        MultiValuedAttribute ims01 = new MultiValuedAttribute.Builder().setValue("ims01").setType("skype").build();
        MultiValuedAttribute ims02 = new MultiValuedAttribute.Builder().setValue("ims02").build();
        List<MultiValuedAttribute> ims = new ArrayList<>();
        ims.add(ims01);
        ims.add(ims02);

        MultiValuedAttribute photo01 = new MultiValuedAttribute.Builder().setValue("photo01.jpg").setType("thumbnail")
                .build();
        MultiValuedAttribute photo02 = new MultiValuedAttribute.Builder().setValue("photo02.jpg").build();
        List<MultiValuedAttribute> photos = new ArrayList<>();
        photos.add(photo01);
        photos.add(photo02);

        MultiValuedAttribute role01 = new MultiValuedAttribute.Builder().setValue("role01").build();
        MultiValuedAttribute role02 = new MultiValuedAttribute.Builder().setValue("role02").build();
        List<MultiValuedAttribute> roles = new ArrayList<>();
        roles.add(role01);
        roles.add(role02);

        MultiValuedAttribute certificate01 = new MultiValuedAttribute.Builder().setValue("certificate01").build();
        MultiValuedAttribute certificate02 = new MultiValuedAttribute.Builder().setValue("certificate02").build();
        List<MultiValuedAttribute> certificates = new ArrayList<>();
        certificates.add(certificate01);
        certificates.add(certificate02);

        Name name = new Name.Builder().setFamilyName("familiyName").setFormatted("formatted Name")
                .setGivenName("givenName").build();

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
                .setIms(ims)
                .setPhotos(photos)
                .setRoles(roles)
                .setName(name)
                .setX509Certificates(certificates)
                .setEntitlements(entitlements)
                .setExternalId("irgendwas");
        User newUser = userBuilder.build();

        originalUser = oConnector.createUser(newUser, accessToken);
        idExistingUser = originalUser.getId();
    }

    private void createUpdateUserWithUpdateFields() {
        Name newName = new Name.Builder().setFamilyName("FamilyName").build();
        updateUser = new UpdateUser.Builder()
                .updateUserName("UserName")
                .updateNickName("NickName")
                .updateExternalId("ExternalId")
                .updateDisplayName("DisplayName")
                .updatePassword("Password")
                .updateLocale("Locale")
                .updatePreferredLanguage("PreferredLanguage")
                .updateProfileUrl("ProfileUrl")
                .updateTimezone("Timezone")
                .updateTitle("Title")
                .updateUserType("UserType")
                .updateExternalId("ExternalId")
                .updateName(newName)
                .updateActive(true).build();
    }

    private void createUpdateUserWithUpdateFieldsWithoutPassword() {
        Name newName = new Name.Builder().setFamilyName("newFamilyName").build();
        updateUser = new UpdateUser.Builder()

                .updateUserName(IRRELEVANT)
                .updateNickName(IRRELEVANT)
                .updateExternalId(IRRELEVANT)
                .updateDisplayName(IRRELEVANT)
                .updateLocale(IRRELEVANT)
                .updatePreferredLanguage(IRRELEVANT)
                .updateProfileUrl(IRRELEVANT)
                .updateTimezone(IRRELEVANT)
                .updateTitle(IRRELEVANT)
                .updateUserType(IRRELEVANT)
                .updateName(newName)
                .updateActive(true).build();
    }

    private void createUpdateUserWithDeleteFields() {
        updateUser = new UpdateUser.Builder()
                .deleteDisplayName()
                .deleteNickName()
                .deleteLocal()
                .deletePreferredLanguage()
                .deleteProfileUrl()
                .deleteTimezone()
                .deleteTitle()
                .deleteUserType()
                .deleteName()
                .deleteExternalId()
                .build();
    }

    private void createUpdateUserWithMultiDeleteFields() {

        Address deleteAddress = new Address.Builder().setCountry("de").setFormatted("formated address 01")
                .setLocality("Berlin").setPostalCode("111111").build();

        MultiValuedAttribute email = new MultiValuedAttribute.Builder().setValue("hsimpson@atom-example.com")
                .setType("work").build();

        MultiValuedAttribute entitlement = new MultiValuedAttribute.Builder().setValue("right2").build();

        MultiValuedAttribute ims = new MultiValuedAttribute.Builder().setValue("ims01").setType("skype").build();

        MultiValuedAttribute phoneNumber = new MultiValuedAttribute.Builder().setValue("0245817964").setType("work")
                .build();

        MultiValuedAttribute photo = new MultiValuedAttribute.Builder().setValue("photo01.jpg").setType("thumbnail")
                .build();

        MultiValuedAttribute x509Certificate = new MultiValuedAttribute.Builder().setValue("certificate01").build();

        updateUser = new UpdateUser.Builder()
                .deleteEmail(email)
                .deleteEntitlement(entitlement)
                .deleteIms(ims)
                .deletePhoneNumber(phoneNumber)
                .deletePhoto(photo)
                .deleteRole("role01")
                .deleteX509Certificate(x509Certificate)
                .deleteAddress(deleteAddress)
                .build();
    }

    private void createUpdateUserWithMultiAddFields() {

        MultiValuedAttribute email = new MultiValuedAttribute.Builder()
                .setValue("mac@muster.de").setType("home").build();

        MultiValuedAttribute phonenumber = new MultiValuedAttribute.Builder()
                .setValue("99999999991").setType("home").build();

        Address newSimpleAddress = new Address.Builder().setCountry("fr").setFormatted("new Address")
                .setLocality("New City").setPostalCode("66666").build();
        MultiValuedAttribute entitlement = new MultiValuedAttribute.Builder().setValue("right3").build();
        MultiValuedAttribute ims = new MultiValuedAttribute.Builder().setValue("ims03").build();
        MultiValuedAttribute photo = new MultiValuedAttribute.Builder().setValue("photo03.jpg").build();
        MultiValuedAttribute role = new MultiValuedAttribute.Builder().setValue("role03").build();
        MultiValuedAttribute certificate = new MultiValuedAttribute.Builder().setValue("certificate03").build();

        updateUser = new UpdateUser.Builder()
                .addEmail(email)
                .addPhoneNumber(phonenumber)
                .addAddress(newSimpleAddress)
                .addEntitlement(entitlement)
                .addIms(ims)
                .addPhoto(photo)
                .addRole(role)
                .addX509Certificate(certificate)// TODO at the second run it will fail
                .build();
    }

    private void createUpdateUserWithMultiAllDeleteFields() {

        updateUser = new UpdateUser.Builder()
                .deleteEmails()
                .deleteAddresses()
                .deleteEntitlements()
                .deleteIms()
                .deletePhoneNumbers()
                .deletePhotos()
                .deleteRoles()
                .deleteX509Certificates()
                .build();
    }

    private void createUpdateUserWithJustOtherNickname() {
        updateUser = new UpdateUser.Builder()
                .updateNickName(IRRELEVANT)
                .build();
    }

    private void createUpdateUserWithNewPrimaryEmailAddress() {

        MultiValuedAttribute primaryEmail = new MultiValuedAttribute.Builder().setValue("hsimpson02@atom-example.com")
                .setPrimary(true).build();

        updateUser = new UpdateUser.Builder()
                .addEmail(primaryEmail)
                .build();
    }

    private void createUpdateUserWithEmptyUserName() {
        updateUser = new UpdateUser.Builder().updateUserName("")
                .build();
    }

    private void createUpdateUserWithNewUserName(String username) {
        updateUser = new UpdateUser.Builder().updateUserName(username)
                .build();
    }

    private void updateUser() {
        returnUser = oConnector.updateUser(idExistingUser, updateUser, accessToken);
        // also get user again from database to be able to compare with return object
        databaseUser = oConnector.getUser(returnUser.getId(), accessToken);
        /*
         * TODO: Uncomment once returnUser and databaseUser are consistent!
         */
        // assertTrue(returnUser.equals(databaseUser));
    }

    private void makeNewConnectionWithNewPassword() {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("UserName").
                setPassword("Password").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
    }

    private AccessToken retrieveNewAccessToken() {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        return oConnector.retrieveAccessToken();
    }

    private Address getAddress(List<Address> addresses, String formatted) {
        Address returnAddress = null;
        if (addresses != null) {
            for (Address actAddress : addresses) {
                if (actAddress.getFormatted().equals(formatted)) {
                    returnAddress = actAddress;
                    break;
                }
            }
        }
        if (returnAddress == null) {
            fail("The address with the formatted part of " + formatted + " could not be found");
        }
        return returnAddress;
    }

}
