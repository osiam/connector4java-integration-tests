package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

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
public class UserServiceIT extends AbstractIntegrationTestBase {


    private UUID validUUID = null;

    private OsiamUserService service;
    private User deserializedUser;

    @Before
    public void setUp() throws Exception {

        service = new OsiamUserService.Builder(endpointAddress).build();

    }

    @Test
    public void get_a_valid_user() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();
        assertEquals(validUUID.toString(), deserializedUser.getId());
    }



    @Test
    public void name_is_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
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
        givenAValidUserUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        MultiValuedAttribute email = emails.get(0);

        assertEquals("barbara@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
    }

    @Test
    public void photos_are_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> photos = deserializedUser.getPhotos();
        assertEquals(1, photos.size());
        MultiValuedAttribute photo = photos.get(0);

        assertEquals("http://example.com/barbara.jpg", photo.getValue().toString());
        assertEquals("photo", photo.getType());
    }

    @Test
    public void ims_are_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> ims = deserializedUser.getIms();
        assertEquals(1, ims.size());
        MultiValuedAttribute im = ims.get(0);

        assertEquals("barbara", im.getValue().toString());
        assertEquals("xmpp", im.getType());
    }

    @Test
    public void phonenumbers_are_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> phonenumbers = deserializedUser.getPhoneNumbers();
        assertEquals(1, phonenumbers.size());
        MultiValuedAttribute phonenumber = phonenumbers.get(0);

        assertEquals("555-555-8377", phonenumber.getValue().toString());
        assertEquals("work", phonenumber.getType());
    }

    @Test
    public void external_id_is_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

    }

    @Test
    public void ensure_basic_values_are_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
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
        givenAValidUserUUID();
        whenUserIsDeserialized();
        assertEquals(null, deserializedUser.getPassword());
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_user_raises_exception() throws Exception {
        service.getUserByUUID(UUID.fromString(INVALID_UUID), accessToken);
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAValidUserUUID();
        given_an_invalid_access_token();

        whenUserIsDeserialized();
        fail();
    }

    private void whenUserIsDeserialized() {
        deserializedUser = service.getUserByUUID(validUUID, accessToken);
    }

    private void givenAValidUserUUID() throws Exception {
        validUUID = UUID.fromString(VALID_USER_UUID);
    }
}