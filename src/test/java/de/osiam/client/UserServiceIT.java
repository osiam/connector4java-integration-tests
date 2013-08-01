package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.AuthService;
import org.osiam.client.oauth.GrantType;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class UserServiceIT {

    private static final String VALID_USER_UUID = "f04f1022-8870-469a-ad56-c3209aebf8eb";
    private static final String INVALID_UUID = "ffffffff-ffff-ffff-ffff-fffffffffff";
    private static final String COUNTRY = "Germany";

    private AccessToken accessToken;
    private UUID validUUID = null;
    private String endpointAddress = "http://localhost:8080/osiam-server";

    private String clientId = "example-client";
    private String clientSecret = "secret";
    private AuthService authService;
    private OsiamUserService service;
    private User deserializedUser;

    @Before
    public void setUp() throws Exception {

        AuthService.Builder authBuilder = new AuthService.Builder(endpointAddress).
                withClientId(clientId).
                withClientSecret(clientSecret).
                withGrantType(GrantType.PASSWORD).
                withUsername("marissa").
                withPassword("koala");
        authService = authBuilder.build();
        service = new OsiamUserService.Builder(endpointAddress).build();
        accessToken = authService.retrieveAccessToken();
    }

    @Test
    public void get_a_valid_user() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();
        assertEquals(validUUID.toString(), deserializedUser.getId());
    }

    @Test
    public void metadata_is_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        Meta deserializedMeta = deserializedUser.getMeta();
        Date expectedCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-07-31 21:43:18");
        Date expectedModified = expectedCreated;

        assertEquals(expectedCreated, deserializedMeta.getCreated());
        assertEquals(expectedModified, deserializedMeta.getLastModified());
        assertEquals(null, deserializedMeta.getLocation());
        assertEquals(null, deserializedMeta.getVersion());
        assertEquals("User", deserializedMeta.getResourceType());
    }

    @Test
    public void address_is_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        List<Address> addresses = deserializedUser.getAddresses();
        assertEquals(1, addresses.size());
        Address address = addresses.get(0);

        assertEquals("example street 42", address.getStreetAddress());
        assertEquals("11111", address.getPostalCode());
        assertEquals(COUNTRY, address.getCountry());
        assertEquals(COUNTRY, address.getRegion());
        assertEquals(COUNTRY, address.getLocality());
    }

    @Test
    public void name_is_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        Name name = deserializedUser.getName();

        assertEquals("Jensen", name.getFamilyName());
        assertEquals("Ms. Barbara J Jensen III", name.getFormatted());
        assertEquals("Barbara", name.getGivenName());
        assertEquals(null, name.getHonorificPrefix());
        assertEquals(null, name.getHonorificSuffix());
        assertEquals(null, name.getMiddleName());
    }

    @Test
    public void emails_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        MultiValuedAttribute email = emails.get(0);

        assertEquals("barbara@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
    }

    @Test
    public void photos_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> photos = deserializedUser.getPhotos();
        assertEquals(1, photos.size());
        MultiValuedAttribute photo = photos.get(0);

        assertEquals("http://example.com/barbara.jpg", photo.getValue().toString());
        assertEquals("photo", photo.getType());
    }

    @Test
    public void ims_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> ims = deserializedUser.getIms();
        assertEquals(1, ims.size());
        MultiValuedAttribute im = ims.get(0);

        assertEquals("barbara", im.getValue().toString());
        assertEquals("xmpp", im.getType());
    }

    @Test
    public void phonenumbers_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> phonenumbers = deserializedUser.getPhoneNumbers();
        assertEquals(1, phonenumbers.size());
        MultiValuedAttribute phonenumber = phonenumbers.get(0);

        assertEquals("555-555-8377", phonenumber.getValue().toString());
        assertEquals("work", phonenumber.getType());
    }

    @Test
    public void external_id_is_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

    }

    @Test
    public void ensure_basic_values_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        assertEquals("bjensen", deserializedUser.getExternalId());
        assertEquals(null, deserializedUser.isActive());
        assertEquals("BarbaraJ.", deserializedUser.getDisplayName());
        assertEquals("de", deserializedUser.getLocale());
        assertEquals("Barbara", deserializedUser.getNickName());
        assertEquals("de", deserializedUser.getPreferredLanguage());
        assertEquals("http://babaraJ.com", deserializedUser.getProfileUrl());
        assertEquals("UTC", deserializedUser.getTimezone());
        assertEquals("Dr.", deserializedUser.getTitle());
        assertEquals("bjensen", deserializedUser.getUserName());
        assertEquals("user", deserializedUser.getUserType());
    }

    @Test
    public void password_is_not_transferred() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();
        assertEquals(null, deserializedUser.getPassword());
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_user_raises_exception() throws Exception {
        service.getUserByUUID(UUID.fromString("b01e0710-e9b9-4181-995f-4f1f59dc2999"), accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAValidUUID();
        given_an_invalid_access_token();

        whenUserIsDeserialized();
        fail();
    }

    private void whenUserIsDeserialized() {
        deserializedUser = service.getUserByUUID(validUUID, accessToken);
    }

    private void given_an_invalid_access_token() throws Exception {
        accessToken = new AccessToken();
        Field tokenField = accessToken.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(accessToken, INVALID_UUID);
        tokenField.setAccessible(false);
    }

    private void givenAValidUUID() throws Exception {
        validUUID = UUID.fromString(VALID_USER_UUID);
    }
}