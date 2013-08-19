package de.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.AuthService;
import org.osiam.client.oauth.GrantType;
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
public class MeUserServiceIT extends AbstractIntegrationTestBase {

    private OsiamUserService service;
    private User deserializedUser;

    @Before
    public void setUp() throws Exception {
        AuthService.Builder authBuilder = new AuthService.Builder(endpointAddress).
                withClientId(clientId).
                withClientSecret(clientSecret).
                withGrantType(GrantType.PASSWORD).
                withUsername("bjensen").
                withPassword("koala");
        authService = authBuilder.build();
        accessToken = authService.retrieveAccessToken();
        service = new OsiamUserService.Builder(endpointAddress).build();
    }

    @Test
    public void name_is_deserialized_correctly() throws Exception {
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
        whenUserIsDeserialized();

        List<MultiValuedAttribute> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        MultiValuedAttribute email = emails.get(0);

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
    }

    @Test
    public void password_is_not_transferred() throws Exception {
        whenUserIsDeserialized();
        assertNull(deserializedUser.getPassword());
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
        givenAnInvalidAccessToken();

        whenUserIsDeserialized();
        fail();
    }

    private void whenUserIsDeserialized() {
        deserializedUser = service.getMe(accessToken);
    }
}