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
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    private AccessToken accessToken;
    private UUID validUUID = null;
    private String endpointAddress = "http://localhost:8080/osiam-server";

    private String clientId = "example-client";
    private String clientSecret = "secret";
    private AuthService authService;
    private OsiamUserService service;

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
        givenAnExistingUserWithUUID();
        User user = service.getUserByUUID(validUUID, accessToken);
        assertEquals(validUUID.toString(), user.getId());
    }

    @Test
    public void ensure_all_values_are_deserialized_correctly() throws Exception {
        givenAnExistingUserWithUUID();
        User actualUser = service.getUserByUUID(validUUID, accessToken);

        assertEquals("User", actualUser.getMeta().getResourceType());
        Date created = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse("31.21.2013 21:34:18");
        assertEquals(created, actualUser.getMeta().getCreated());
        assertEquals(created, actualUser.getMeta().getLastModified());
        assertEquals(VALID_USER_UUID, actualUser.getId());
        assertEquals(1, actualUser.getAddresses().size());
        assertEquals("example street 42", actualUser.getAddresses().get(0).getStreetAddress());
        assertEquals("germany", actualUser.getAddresses().get(0).getCountry());
        assertEquals("11111", actualUser.getAddresses().get(0).getPostalCode());
        assertEquals("MaxExample", actualUser.getDisplayName());
        assertEquals(2, actualUser.getEmails().size());
        String email = actualUser.getEmails().get(0).getValue().toString();
        boolean exists = email.equals("MaxExample@work.com") || email.equals("MaxExample@home.de");
        assertTrue(exists);
        email = actualUser.getEmails().get(1).getValue().toString();
        exists = email.equals("MaxExample@work.com") || email.equals("MaxExample@home.de");
        assertTrue(exists);
        assertEquals("MExample", actualUser.getExternalId());
        assertEquals("de", actualUser.getLocale());
        assertEquals("Example", actualUser.getName().getFamilyName());
        assertEquals("Max", actualUser.getName().getGivenName());
        assertEquals("Jason", actualUser.getName().getMiddleName());
        assertEquals("Max", actualUser.getNickName());
        assertEquals(null, actualUser.getPassword());
        assertEquals(1, actualUser.getPhoneNumbers().size());
        assertEquals("666-999-6666", actualUser.getPhoneNumbers().get(0).getValue().toString());
        assertEquals("de", actualUser.getPreferredLanguage());
        assertEquals("http://test.de", actualUser.getProfileUrl());
        assertEquals("UTC", actualUser.getTimezone());
        assertEquals("Dr", actualUser.getTitle());
        assertEquals("MaxExample", actualUser.getUserName());
        assertEquals("User", actualUser.getUserType());
        assertEquals(null, actualUser.isActive());
        assertEquals(1, actualUser.getPhotos().size());
        assertEquals("photo", actualUser.getPhotos().get(0).getType());
        assertEquals("https://photos.example.com/profilephoto/72930000000Ccne.jpg"
                , actualUser.getPhotos().get(0).getValue().toString());
        assertEquals(1, actualUser.getIms().size());
        assertEquals("someaimhandle", actualUser.getIms().get(0).getValue().toString());
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_user_raises_exception() throws Exception {
        service.getUserByUUID(UUID.fromString("b01e0710-e9b9-4181-995f-4f1f59dc2999"), accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAnExistingUserWithUUID();
        given_an_invalid_access_token();

        service.getUserByUUID(validUUID, accessToken);
        fail();
    }

    private void given_an_invalid_access_token() throws Exception {
        accessToken = new AccessToken();
        Field tokenField = accessToken.getClass().getDeclaredField("token");
        tokenField.setAccessible(true);
        tokenField.set(accessToken, INVALID_UUID);
        tokenField.setAccessible(false);
    }

    private void givenAnExistingUserWithUUID() throws Exception {
        validUUID = UUID.fromString(VALID_USER_UUID);
    }
}