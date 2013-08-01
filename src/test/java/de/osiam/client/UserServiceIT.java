package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.AuthService;
import org.osiam.client.oauth.GrantType;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Meta;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;


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
    public void ensure_metadata_is_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();
        Meta deserializedMeta = deserializedUser.getMeta();
        Date expectedCreated = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-07-31 21:43:18");
        Date expectedModified = expectedCreated;
        assertEquals(expectedCreated, deserializedMeta.getCreated());
    }

    @Test
    public void ensure_address_is_deserialized_correctly() throws Exception {
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
    @Ignore
    public void ensure_all_values_are_deserialized_correctly() throws Exception {
        givenAValidUUID();
        whenUserIsDeserialized();

        assertEquals("User", deserializedUser.getMeta().getResourceType());

        assertEquals("bjensen", deserializedUser.getDisplayName());
        assertEquals(2, deserializedUser.getEmails().size());
        String email = deserializedUser.getEmails().get(0).getValue().toString();
        boolean exists = email.equals("MaxExample@work.com") || email.equals("MaxExample@home.de");
        assertTrue(exists);
        email = deserializedUser.getEmails().get(1).getValue().toString();
        exists = email.equals("MaxExample@work.com") || email.equals("MaxExample@home.de");
        assertTrue(exists);
        assertEquals("MExample", deserializedUser.getExternalId());
        assertEquals("de", deserializedUser.getLocale());
        assertEquals("Example", deserializedUser.getName().getFamilyName());
        assertEquals("Max", deserializedUser.getName().getGivenName());
        assertEquals("Jason", deserializedUser.getName().getMiddleName());
        assertEquals("Max", deserializedUser.getNickName());
        assertEquals(null, deserializedUser.getPassword());
        assertEquals(1, deserializedUser.getPhoneNumbers().size());
        assertEquals("666-999-6666", deserializedUser.getPhoneNumbers().get(0).getValue().toString());
        assertEquals("de", deserializedUser.getPreferredLanguage());
        assertEquals("http://test.de", deserializedUser.getProfileUrl());
        assertEquals("UTC", deserializedUser.getTimezone());
        assertEquals("Dr", deserializedUser.getTitle());
        assertEquals("bjensen", deserializedUser.getUserName());
        assertEquals("User", deserializedUser.getUserType());
        assertEquals(null, deserializedUser.isActive());
        assertEquals(1, deserializedUser.getPhotos().size());
        assertEquals("photo", deserializedUser.getPhotos().get(0).getType());
        assertEquals("https://photos.example.com/profilephoto/72930000000Ccne.jpg"
                , deserializedUser.getPhotos().get(0).getValue().toString());
        assertEquals(1, deserializedUser.getIms().size());
        assertEquals("someaimhandle", deserializedUser.getIms().get(0).getValue().toString());
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