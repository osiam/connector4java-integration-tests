package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.query.QueryBuilder;
import org.osiam.client.query.QueryResult;
import org.osiam.resources.scim.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
    }

    @Test
    public void external_id_is_deserialized_correctly() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();

    }

    @Test
    public void password_is_not_transferred() throws Exception {
        givenAValidUserUUID();
        whenUserIsDeserialized();
        assertEquals(null, deserializedUser.getPassword());
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