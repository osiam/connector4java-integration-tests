package de.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.User;
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
public class UserServiceIT extends AbstractIntegrationTestBase {

    private String validUUID = null;
    private User deserializedUser;

    @Test
    public void name_is_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

        Name name = deserializedUser.getName();

        assertEquals("Jensen", name.getFamilyName());
        assertEquals("Ms. Barbara J Jensen III", name.getFormatted());
        assertEquals("Barbara", name.getGivenName());
        assertNull(name.getHonorificPrefix());
        assertNull(name.getHonorificSuffix());
        assertNull(name.getMiddleName());
    }

    @Test
    public void emails_are_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

        List<MultiValuedAttribute> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        MultiValuedAttribute email = emails.get(0);

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
    }

    @Test
    public void password_is_not_transferred() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();
        assertNull(deserializedUser.getPassword());
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAValidUserUUID();
        givenAnInvalidAccessToken();

        whenUserIsDeserialized();
        fail();
    }

    @Test(expected = UnauthorizedException.class)
    public void access_token_is_expired() throws Exception {
    	givenAValidUserUUID();
    	givenAnAccessTokenForOneSecond();
    	Thread.sleep(1000);
        whenUserIsDeserialized();
        fail();
    }
    
    private void whenUserIsDeserialized() {
        deserializedUser = oConnector.getUser(validUUID, accessToken);
    }

    private void givenAValidUserUUID() throws Exception {
        validUUID = VALID_USER_UUID;
    }
}