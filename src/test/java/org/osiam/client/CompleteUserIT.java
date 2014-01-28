package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Entitlement;
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.Im;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.PhoneNumber;
import org.osiam.resources.scim.Photo;
import org.osiam.resources.scim.Role;
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
@DatabaseSetup("/database_seed_complete_user.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class CompleteUserIT extends AbstractIntegrationTestBase {

    private static final String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    private static final String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private static final String EXTENSION_URN = "extension";

    @Test
    public void create_complete_user_works() {
        User newUser = initializeUserWIthAllAttributes();
        User retUser = oConnector.createUser(newUser, accessToken);
        User dbUser = oConnector.getUser(retUser.getId(), accessToken);
        assertThatNewUserAndReturnUserAreEqual(newUser, dbUser);
    }

    @Ignore
    @Test
    public void update_all_attributes_of_one_user_works() {
        // UpdateUser updateUser
    }

    private UpdateUser createUpdateUser() {
        UpdateUser.Builder updateUserBuilder = new UpdateUser.Builder();

        updateUserBuilder.updateActive(false);
        // updateUserBuilder.updateAddress(oldAttribute, newAttribute)
        return null;
    }

    private User initializeUserWIthAllAttributes() {

        List<Address> addresses = new ArrayList<Address>();
        Address address = new Address.Builder().setCountry("Germany")
                .setFormatted("formatted").setLocality("Berlin")
                .setPostalCode("12345").setPrimary(true).setRegion("Berlin")
                .setStreetAddress("Voltastr. 5").setType(Address.Type.WORK)
                .build();
        addresses.add(address);
        List<Email> emails = new ArrayList<Email>();
        Email email = new Email.Builder().setPrimary(true)
                .setValue("test@tarent.de").setType(Email.Type.WORK).build();
        emails.add(email);
        List<Entitlement> entitlements = new ArrayList<Entitlement>();
        Entitlement entitlement = new Entitlement.Builder().setPrimary(true)
                .setType(new Entitlement.Type("irrelevant"))
                .setValue("entitlement").build();
        entitlements.add(entitlement);
        List<Im> ims = new ArrayList<Im>();
        Im im = new Im.Builder().setPrimary(true).setType(Im.Type.AIM)
                .setValue("aim").build();
        ims.add(im);
        Name name = new Name.Builder().setFamilyName("test")
                .setFormatted("formatted").setGivenName("test")
                .setHonorificPrefix("Dr.").setHonorificSuffix("Mr.")
                .setMiddleName("test").build();
        List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
        PhoneNumber phoneNumber = new PhoneNumber.Builder().setPrimary(true)
                .setType(PhoneNumber.Type.WORK).setValue("03012345678").build();
        phoneNumbers.add(phoneNumber);
        List<Photo> photos = new ArrayList<Photo>();
        Photo photo = new Photo.Builder().setPrimary(true)
                .setType(Photo.Type.PHOTO).setValue("username.jpg").build();
        photos.add(photo);
        List<Role> roles = new ArrayList<Role>();
        Role role = new Role.Builder().setPrimary(true).setValue("user_role")
                .build();
        roles.add(role);
        List<X509Certificate> x509Certificates = new ArrayList<X509Certificate>();
        X509Certificate x509Certificat = new X509Certificate.Builder()
                .setPrimary(true).setValue("x509Certificat").build();
        x509Certificates.add(x509Certificat);
        Extension extension = new Extension(EXTENSION_URN);
        extension.addOrUpdateField("gender", "female");
        extension.addOrUpdateField("age", new BigInteger("18"));
        return new User.Builder("complete_add_user").setActive(true)
                .setAddresses(addresses).setDisplayName("displayName")
                .setEmails(emails).setEntitlements(entitlements)
                .setExternalId("externalId").setIms(ims).setLocale("de_DE")
                .setName(name).setNickName("nickname").setPassword("password")
                .setPhoneNumbers(phoneNumbers).setPhotos(photos)
                .setPreferredLanguage("german").setProfileUrl("/user/username")
                .setRoles(roles).setTimezone("DE").setTitle("title")
                .setX509Certificates(x509Certificates)
                .addExtension(extension)
                .build();
    }

    private void assertThatNewUserAndReturnUserAreEqual(User expectedUser, User actualUser) {
        assertThatAddressesAreEqual(expectedUser.getAddresses(), actualUser.getAddresses());
        assertEquals(expectedUser.getAllExtensions(), actualUser.getAllExtensions());
        assertEquals(expectedUser.getDisplayName(), actualUser.getDisplayName());
        assertThatEmailsAreEqual(expectedUser.getEmails(), actualUser.getEmails());
        assertThatEntitlementsAreEqual(expectedUser.getEntitlements(), actualUser.getEntitlements());
        assertEquals(expectedUser.getExternalId(), actualUser.getExternalId());
        assertThatImsAreEqual(expectedUser.getIms(), actualUser.getIms());
        assertEquals(expectedUser.getLocale(), actualUser.getLocale());
        assertThatNamesAreEqual(expectedUser.getName(), actualUser.getName());
        assertEquals(expectedUser.getNickName(), actualUser.getNickName());
        assertThatPhoneNumbersAreEqual(expectedUser.getPhoneNumbers(), actualUser.getPhoneNumbers());
        assertThatPhotosAreEqual(expectedUser.getPhotos(), actualUser.getPhotos());
        assertEquals(expectedUser.getPreferredLanguage(), actualUser.getPreferredLanguage());
        assertEquals(expectedUser.getProfileUrl(), actualUser.getProfileUrl());
        assertThatRolesAreEqual(expectedUser.getRoles(), actualUser.getRoles());
        assertEquals(expectedUser.getTimezone(), actualUser.getTimezone());
        assertEquals(expectedUser.getTitle(), actualUser.getTitle());
        assertEquals(expectedUser.getUserName(), actualUser.getUserName());
        assertEquals(expectedUser.getUserType(), actualUser.getUserType());
        assertThatX509CertificatesAreEqual(expectedUser.getX509Certificates(), actualUser.getX509Certificates());
        assertEquals(expectedUser.isActive(), actualUser.isActive());
    }

    private void assertThatRolesAreEqual(List<Role> expected, List<Role> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Role expectedValue = expected.get(0);
        Role actualValue = actual.get(0);

        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatNamesAreEqual(Name expected, Name actual) {
        assertEquals(expected.getFamilyName(), actual.getFamilyName());
        assertEquals(expected.getFormatted(), actual.getFormatted());
        assertEquals(expected.getGivenName(), actual.getGivenName());
        assertEquals(expected.getHonorificPrefix(), actual.getHonorificPrefix());
        assertEquals(expected.getHonorificSuffix(), actual.getHonorificSuffix());
        assertEquals(expected.getMiddleName(), actual.getMiddleName());
    }

    private void assertThatX509CertificatesAreEqual(List<X509Certificate> expected, List<X509Certificate> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        X509Certificate expectedValue = expected.get(0);
        X509Certificate actualValue = actual.get(0);

        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatPhotosAreEqual(List<Photo> expected, List<Photo> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Photo expectedValue = expected.get(0);
        Photo actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatPhoneNumbersAreEqual(List<PhoneNumber> expected, List<PhoneNumber> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        PhoneNumber expectedValue = expected.get(0);
        PhoneNumber actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatEmailsAreEqual(List<Email> expected, List<Email> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Email expectedValue = expected.get(0);
        Email actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatImsAreEqual(List<Im> expected, List<Im> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Im expectedValue = expected.get(0);
        Im actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatEntitlementsAreEqual(List<Entitlement> expected, List<Entitlement> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Entitlement expectedValue = expected.get(0);
        Entitlement actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.getValue(), actualValue.getValue());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
    }

    private void assertThatAddressesAreEqual(List<Address> expected, List<Address> actual) {
        assertEquals(expected.size(), actual.size());
        ensureListSizeIsOne(expected);
        Address expectedValue = expected.get(0);
        Address actualValue = actual.get(0);

        assertEquals(expectedValue.getType(), actualValue.getType());
        assertEquals(expectedValue.isPrimary(), actualValue.isPrimary());
        assertEquals(expectedValue.getCountry(), actualValue.getCountry());
        assertEquals(expectedValue.getFormatted(), actualValue.getFormatted());
        assertEquals(expectedValue.getLocality(), actualValue.getLocality());
        assertEquals(expectedValue.getPostalCode(), actualValue.getPostalCode());
        assertEquals(expectedValue.getRegion(), actualValue.getRegion());
        assertEquals(expectedValue.getStreetAddress(), actualValue.getStreetAddress());
    }

    private <T extends MultiValuedAttribute> void ensureListSizeIsOne(List<T> expected) {
        assertTrue("At the moment only lists of the size of one are suported", expected.size() == 1);
    }
}
