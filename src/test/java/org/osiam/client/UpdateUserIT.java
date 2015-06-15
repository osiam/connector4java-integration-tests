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

import static org.hamcrest.CoreMatchers.containsString;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Entitlement;
import org.osiam.resources.scim.Im;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.PhoneNumber;
import org.osiam.resources.scim.Photo;
import org.osiam.resources.scim.Role;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;
import org.osiam.resources.scim.X509Certificate;
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
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class UpdateUserIT extends AbstractIntegrationTestBase {

    private static final String IRRELEVANT = "Irrelevant";
    private static final String NOT_EXISTING_ID = "e15e8991-aab7-4835-a448-7b873e69b86c";

    private String idExistingUser = "7d33bcbe-a54c-43d8-867e-f6146164941e";
    private UpdateUser updateUser;
    private User testUser;
    private User returnUser;
    private User databaseUser;

    @Before
    public void setUp() {
        retrieveAccessTokenForMarissa();
    }

    @Test
    public void delete_multi_value_attributes() {
        createFullUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();

        assertFalse(isValuePartOfEmailList(returnUser.getEmails(), "hsimpson@atom-example.com"));
        assertFalse(isValuePartOfPhoneNumberList(returnUser.getPhoneNumbers(), "0245817964"));
        assertFalse(isValuePartOfImList(returnUser.getIms(), "ims01"));
        URI uri = null;
        try {
            uri = new URI("photo01.jpg");
        } catch (Exception e) {
        }

        assertFalse(isValuePartOfPhotoList(returnUser.getPhotos(), uri));
        assertFalse(isValuePartOfRoleList(returnUser.getRoles(), "role01"));
        assertFalse(isValuePartOfAddressList(returnUser.getAddresses(), "formated address 01"));
        assertFalse(isValuePartOfEntitlementList(returnUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfX509CertificateList(returnUser.getX509Certificates(), "certificate01"));
    }

    @Test
    @Ignore("write a private equals method to compare both users for value-equality")
    public void compare_returned_user_with_database_user() {
        createFullUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();

        assertTrue(returnUser.equals(databaseUser));
    }

    @Test
    public void REGT_015_delete_multi_value_attributes_twice() {
        createFullUser("dma");
        createUpdateUserWithMultiDeleteFields();

        updateUser();
        updateUser();

        assertTrue(isValuePartOfEntitlementList(testUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfEntitlementList(returnUser.getEntitlements(), "right2"));
        assertFalse(isValuePartOfEntitlementList(databaseUser.getEntitlements(), "right2"));
        assertTrue(isValuePartOfX509CertificateList(testUser.getX509Certificates(), "certificate01"));
        assertFalse(isValuePartOfX509CertificateList(returnUser.getX509Certificates(), "certificate01"));
        assertFalse(isValuePartOfX509CertificateList(databaseUser.getX509Certificates(), "certificate01"));
    }

    @Test
    public void delete_all_multi_value_attributes() {
        createFullUser("dama");
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
    public void add_multi_value_attributes() {
        createFullUser("ama");
        createUpdateUserWithMultiAddFields();

        updateUser();

        assertEquals(testUser.getPhoneNumbers().size() + 1, returnUser.getPhoneNumbers().size());
        assertTrue(isValuePartOfPhoneNumberList(returnUser.getPhoneNumbers(), "99999999991"));
        assertEquals(testUser.getEmails().size() + 1, returnUser.getEmails().size());
        assertTrue(isValuePartOfEmailList(returnUser.getEmails(), "mac@muster.de"));
        assertEquals(testUser.getAddresses().size() + 1, returnUser.getAddresses().size());
        getAddress(returnUser.getAddresses(), "new Address");
        assertEquals(testUser.getEntitlements().size() + 1, returnUser.getEntitlements().size());
        assertTrue(isValuePartOfEntitlementList(returnUser.getEntitlements(), "right3"));
        assertEquals(testUser.getIms().size() + 1, returnUser.getIms().size());
        assertTrue(isValuePartOfImList(returnUser.getIms(), "ims03"));
        assertEquals(testUser.getPhotos().size() + 1, returnUser.getPhotos().size());
        URI uri = null;
        try {
            uri = new URI("photo03.jpg");
        } catch (Exception e) {
        }

        assertTrue(isValuePartOfPhotoList(returnUser.getPhotos(), uri));
        assertEquals(testUser.getRoles().size() + 1, returnUser.getRoles().size());
        assertTrue(isValuePartOfRoleList(returnUser.getRoles(), "role03"));
        assertEquals(testUser.getX509Certificates().size() + 1, returnUser.getX509Certificates().size());
        assertTrue(isValuePartOfX509CertificateList(returnUser.getX509Certificates(), "certificate03"));
    }

    @Test
    public void update_all_single_values() {
        createFullUser("uasv");
        createUpdateUserWithUpdateFields();

        updateUser();

        assertEquals("UserName", returnUser.getUserName());
        assertEquals("NickName", returnUser.getNickName());
        assertNotEquals(testUser.isActive(), returnUser.isActive());
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
        createFullUser("desv");
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
        createFullUser("uasv");
        createUpdateUserWithUpdateFields();

        updateUser();

        OSIAM_CONNECTOR.retrieveAccessToken("UserName", "Password", Scope.ALL);
    }

    @Test
    public void change_one_field_and_other_attributes_are_the_same() {
        createFullUser("cnaoaats");
        createUpdateUserWithJustOtherNickname();

        updateUser();

        assertNotEquals(testUser.getNickName(), returnUser.getNickName());
        assertEquals(testUser.isActive(), returnUser.isActive());
        assertEquals(testUser.getDisplayName(), returnUser.getDisplayName());
        assertEquals(testUser.getExternalId(), returnUser.getExternalId());
        assertEquals(testUser.getLocale(), returnUser.getLocale());
        assertEquals(testUser.getPreferredLanguage(), returnUser.getPreferredLanguage());
        assertEquals(testUser.getProfileUrl(), returnUser.getProfileUrl());
        assertEquals(testUser.getTimezone(), returnUser.getTimezone());
        assertEquals(testUser.getTitle(), returnUser.getTitle());
        assertEquals(testUser.getUserType(), returnUser.getUserType());
        assertEquals(testUser.getName().getFamilyName(), returnUser.getName().getFamilyName());
    }

    @Test
    public void username_is_set_no_empty_string_is_thrown_probably() {
        createFullUser("ietiuuitp");
        createUpdateUserWithEmptyUserName();

        updateUser();

        assertThat(returnUser.getUserName(), is(equalTo(testUser.getUserName())));
    }

    @Test
    public void update_attributes_does_not_change_the_password() {
        createFullUser("uadctp");
        createUpdateUserWithUpdateFieldsWithoutPassword();

        updateUser();

        assertThat(OSIAM_CONNECTOR.retrieveAccessToken(IRRELEVANT, "geheim", Scope.ALL), is(notNullValue()));
    }

    @Test(expected = ConflictException.class)
    public void updating_the_username_to_existing_username_raises_exception() {
        createUpdateUserWithNewUserName("marissa");

        updateUser();
    }

    @Test
    public void adding_new_primary_email_address_sets_the_other_to_non_primary() {
        createFullUser(IRRELEVANT);
        createUpdateUserWithNewPrimaryEmailAddress();

        updateUser();

        assertThatOnlyNewEmailAddressIsPrimary();
    }

    @Test
    public void deleting_and_add_of_same_mail_address_works() {
        createFullUser(IRRELEVANT);
        createUpdateUserWhereTheSameEmailIsSetToDeleteAndAdd();
        updateUser();

        assertThat(testUser.getEmails(), is(databaseUser.getEmails()));
    }

    @Test
    public void replace_user_which_not_existing_raises_exception() {
        retrieveAccessTokenForMarissa();

        UpdateUser patchedUser = new UpdateUser.Builder().build();
        try {
            OSIAM_CONNECTOR.updateUser(NOT_EXISTING_ID, patchedUser, accessToken);
            fail("Exception expected");
        } catch (NoResultException e) {
            assertThat(e.getMessage(), containsString("not found"));
        }
    }

    private void assertThatOnlyNewEmailAddressIsPrimary() {
        for (Email email : returnUser.getEmails()) {
            if (email.getValue().equals("hsimpson02@atom-example.com")) {
                assertThat(email.isPrimary(), is(true));
            } else {
                assertThat(email.isPrimary(), is(false));
            }
        }
    }

    private boolean isValuePartOfEmailList(List<Email> list, String value) {
        if (list != null) {
            for (Email actAttribute : list) {
                if (actAttribute.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfPhoneNumberList(List<PhoneNumber> list, String value) {
        if (list != null) {
            for (PhoneNumber actAttribute : list) {
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

    private boolean isValuePartOfX509CertificateList(
            List<X509Certificate> list, String value) {
        if (list != null) {
            for (X509Certificate actAttribute : list) {
                if (actAttribute.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfEntitlementList(List<Entitlement> list,
            String value) {
        if (list != null) {
            for (Entitlement actAttribute : list) {
                if (actAttribute.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfRoleList(List<Role> list, String value) {
        if (list != null) {
            for (Role actRole : list) {
                if (actRole.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfPhotoList(List<Photo> list, URI uri) {
        if (list != null) {
            for (Photo actPhoto : list) {
                if (actPhoto.getValueAsURI().equals(uri)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValuePartOfImList(List<Im> list, String value) {
        if (list != null) {
            for (Im actIm : list) {
                if (actIm.getValue().equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private User createFullUser(String userName) {
        User.Builder userBuilder = new User.Builder(userName);

        Email email01 = new Email.Builder().setValue("hsimpson@atom-example.com")
                .setType(Email.Type.WORK).setPrimary(true).build();
        Email email02 = new Email.Builder().setValue("hsimpson@home-example.com")
                .setType(Email.Type.WORK).build();
        List<Email> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);

        PhoneNumber phoneNumber01 = new PhoneNumber.Builder().setValue("+497845/1157")
                .setType(PhoneNumber.Type.WORK).setPrimary(true).build();
        PhoneNumber phoneNumber02 = new PhoneNumber.Builder().setValue("0245817964").setType(PhoneNumber.Type.WORK)
                .build();
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber01);
        phoneNumbers.add(phoneNumber02);

        Address simpleAddress01 = new Address.Builder().setCountry("de").setFormatted("formated address 01")
                .setLocality("Berlin").setPostalCode("111111").build();
        Address simpleAddress02 = new Address.Builder().setCountry("en").setFormatted("address formated 02")
                .setLocality("New York").setPostalCode("123456").build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(simpleAddress01);
        addresses.add(simpleAddress02);

        Entitlement entitlement01 = new Entitlement.Builder().setValue("right1").build();
        Entitlement entitlement02 = new Entitlement.Builder().setValue("right2").build();
        List<Entitlement> entitlements = new ArrayList<>();
        entitlements.add(entitlement01);
        entitlements.add(entitlement02);

        Im ims01 = new Im.Builder().setValue("ims01").setType(Im.Type.SKYPE).build();
        Im ims02 = new Im.Builder().setValue("ims02").build();
        List<Im> ims = new ArrayList<>();
        ims.add(ims01);
        ims.add(ims02);

        URI uri1 = null;
        URI uri2 = null;
        try {
            uri1 = new URI("photo01.jpg");
            uri2 = new URI("photo02.jpg");
        } catch (Exception e) {
        }

        Photo photo01 = new Photo.Builder().setValue(uri1).setType(Photo.Type.THUMBNAIL)
                .build();

        Photo photo02 = new Photo.Builder().setValue(uri2).build();
        List<Photo> photos = new ArrayList<>();
        photos.add(photo01);
        photos.add(photo02);

        Role role01 = new Role.Builder().setValue("role01").build();
        Role role02 = new Role.Builder().setValue("role02").build();
        List<Role> roles = new ArrayList<>();
        roles.add(role01);
        roles.add(role02);

        X509Certificate certificate01 = new X509Certificate.Builder().setValue("certificate01").build();
        X509Certificate certificate02 = new X509Certificate.Builder().setValue("certificate02").build();
        List<X509Certificate> certificates = new ArrayList<>();
        certificates.add(certificate01);
        certificates.add(certificate02);

        Name name = new Name.Builder().setFamilyName("familiyName").setFormatted("formatted Name")
                .setGivenName("givenName").build();

        userBuilder.setNickName("irgendwas")
                .addEmails(emails)
                .addPhoneNumbers(phoneNumbers)
                .setActive(false)
                .setDisplayName("irgendwas")
                .setLocale("de")
                .setPassword("geheim")
                .setPreferredLanguage("de")
                .setProfileUrl("irgendwas")
                .setTimezone("irgendwas")
                .setTitle("irgendwas")
                .setUserType("irgendwas")
                .addAddresses(addresses)
                .addIms(ims)
                .addPhotos(photos)
                .addRoles(roles)
                .setName(name)
                .addX509Certificates(certificates)
                .addEntitlements(entitlements)
                .setExternalId("irgendwas");
        User newUser = userBuilder.build();

        testUser = OSIAM_CONNECTOR.createUser(newUser, accessToken);
        idExistingUser = testUser.getId();

        return testUser;
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

        Email email = new Email.Builder().setValue("hsimpson@atom-example.com")
                .setType(Email.Type.WORK).build();

        Entitlement entitlement = new Entitlement.Builder().setValue("right2").build();

        Im ims = new Im.Builder().setValue("ims01").setType(Im.Type.SKYPE).build();

        PhoneNumber phoneNumber = new PhoneNumber.Builder().setValue("0245817964").setType(PhoneNumber.Type.WORK)
                .build();

        URI uri = null;
        try {
            uri = new URI("photo01.jpg");
        } catch (Exception e) {
        }

        Photo photo = new Photo.Builder().setValue(uri).setType(Photo.Type.THUMBNAIL)
                .build();

        X509Certificate x509Certificate = new X509Certificate.Builder().setValue("certificate01").build();

        updateUser = new UpdateUser.Builder()
                .deleteEmail(email)
                .deleteEntitlement(entitlement)
                .deleteIm(ims)
                .deletePhoneNumber(phoneNumber)
                .deletePhoto(photo)
                .deleteRole("role01")
                .deleteX509Certificate(x509Certificate)
                .deleteAddress(deleteAddress)
                .build();
    }

    private void createUpdateUserWithMultiAddFields() {

        Email email = new Email.Builder()
                .setValue("mac@muster.de").setType(Email.Type.HOME).build();

        PhoneNumber phonenumber = new PhoneNumber.Builder()
                .setValue("99999999991").setType(PhoneNumber.Type.HOME).build();

        Address newSimpleAddress = new Address.Builder().setCountry("fr").setFormatted("new Address")
                .setLocality("New City").setPostalCode("66666").build();
        Entitlement entitlement = new Entitlement.Builder().setValue("right3").build();
        Im ims = new Im.Builder().setValue("ims03").build();

        URI uri = null;
        try {
            uri = new URI("photo03.jpg");
        } catch (Exception e) {
        }

        Photo photo = new Photo.Builder().setValue(uri).build();
        Role role = new Role.Builder().setValue("role03").build();
        X509Certificate certificate = new X509Certificate.Builder().setValue("certificate03").build();

        updateUser = new UpdateUser.Builder()
                .addEmail(email)
                .addPhoneNumber(phonenumber)
                .addAddress(newSimpleAddress)
                .addEntitlement(entitlement)
                .addIm(ims)
                .addPhoto(photo)
                .addRole(role)
                .addX509Certificate(certificate)
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

        Email primaryEmail = new Email.Builder().setValue("hsimpson02@atom-example.com")
                .setPrimary(true).build();

        updateUser = new UpdateUser.Builder()
                .addEmail(primaryEmail)
                .build();
    }

    private void createUpdateUserWhereTheSameEmailIsSetToDeleteAndAdd() {
        Email email = testUser.getEmails().get(0);
        updateUser = new UpdateUser.Builder()
                .deleteEmail(email)
                .addEmail(email)
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
        returnUser = OSIAM_CONNECTOR.updateUser(idExistingUser, updateUser, accessToken);
        // get user again from database to be able to compare with return object
        databaseUser = OSIAM_CONNECTOR.getUser(returnUser.getId(), accessToken);
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
