package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.resources.scim.BasicMultiValuedAttribute;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.User;
import org.osiam.resources.type.EmailType;
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

    private String validID = null;
    private User deserializedUser;

    @Test
    public void name_is_deserialized_correctly() throws Exception {
        givenAValidUserID();
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
        givenAValidUserID();
        whenUserIsDeserialized();

        List<Email> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        Email email = emails.get(0);

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals(EmailType.WORK, email.getType());
    }

    @Test
    public void password_is_not_transferred() throws Exception {
        givenAValidUserID();
        whenUserIsDeserialized();
        assertNull(deserializedUser.getPassword());
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAValidUserID();
        givenAnInvalidAccessToken();

        whenUserIsDeserialized();
        fail();
    }

    @Test(expected = UnauthorizedException.class)
    public void access_token_is_expired() throws Exception {
    	givenAValidUserID();
    	givenAnAccessTokenForOneSecond();
    	Thread.sleep(1000);
        whenUserIsDeserialized();
        fail();
    }
    
    private void whenUserIsDeserialized() {
        deserializedUser = oConnector.getUser(validID, accessToken);
    }

    private void givenAValidUserID() throws Exception {
        validID = VALID_USER_ID;
    }
}