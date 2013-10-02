package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
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
public class MeUserServiceIT extends AbstractIntegrationTestBase {

    private User deserializedUser;

    @Test
    public void name_is_deserialized_correctly_for_user_bjensen() throws Exception {
        givenAnAccessTokenForBJensen();
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
    public void get_basic_me_returns_user() throws Exception {
        givenAnAccessTokenForBJensen();
        whenBasicUserIsDeserialized();

        Name name = deserializedUser.getName();

        assertEquals("Jensen", name.getFamilyName());
        assertEquals("Ms. Barbara J Jensen III", name.getFormatted());
        assertEquals("Barbara", name.getGivenName());
        assertNull(name.getHonorificPrefix());
        assertNull(name.getHonorificSuffix());
        assertNull(name.getMiddleName());
    }

    @Test
    public void emails_are_deserialized_correctly_for_user_bjensen() throws Exception {
        givenAnAccessTokenForBJensen();
        whenUserIsDeserialized();

        List<Email> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        Email email = emails.get(0);

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals(EmailType.WORK, email.getType());
    }
    
    @Test
    public void name_is_deserialized_correctly_for_user_hsimpson() throws Exception {
        givenAnAccessTokenForHSimpson();
        whenUserIsDeserialized();

        Name name = deserializedUser.getName();

        assertEquals("Simpson", name.getFamilyName());
        assertEquals("Mr. Homer Simpson", name.getFormatted());
        assertEquals("Homer", name.getGivenName());
        assertNull(name.getHonorificPrefix());
        assertNull(name.getHonorificSuffix());
        assertNull(name.getMiddleName());
    }

    @Test
    public void emails_are_deserialized_correctly_for_user_hsimpson() throws Exception {
        givenAnAccessTokenForHSimpson();
        whenUserIsDeserialized();

        List<Email> emails = sortEmails(deserializedUser.getEmails());
        assertEquals(2, emails.size());

        Email email1 = emails.get(0);
        assertEquals("hsimpson@atom-example.com", email1.getValue().toString());
        assertEquals(EmailType.WORK, email1.getType());

        Email email2 = emails.get(1);
        assertEquals("hsimpson@phome-example.com", email2.getValue().toString());
        assertEquals(EmailType.HOME, email2.getType());
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
    
    @Test(expected = UnauthorizedException.class)
    public void access_token_is_expired() throws Exception {
        givenAnAccessTokenForOneSecond();
        Thread.sleep(1000);
        whenUserIsDeserialized();
        fail();
    }

    @Test
    public void try_to_get_user_after_it_is_deleteted_raises_exception() throws Exception{
    	givenAnAccessTokenForHSimpson();
    	deleteHSimpson();
    	whenUserIsDeserialized();
    }
    
    private void whenBasicUserIsDeserialized() {
        deserializedUser = oConnector.getMe(accessToken);
    }
    
    private void whenUserIsDeserialized() {
        deserializedUser = oConnector.getMe(accessToken);
    }
    
    private void deleteHSimpson(){
    	oConnector.deleteUser("7d33bcbe-a54c-43d8-867e-f6146164941e", accessToken);
    }
    
    private List<Email> sortEmails(List<Email> emails) {
        Collections.sort(emails, new Comparator<Email>() {
            @Override
            public int compare(Email o1, Email o2) {
                return o1.getType().compareTo(o2.getType());
            }
        });
        
        return emails;
    }
    
    private void givenAnAccessTokenForBJensen() throws Exception {
        OsiamConnector.Builder authBuilder = new OsiamConnector.Builder(ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("bjensen").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = authBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }

    private void givenAnAccessTokenForHSimpson() throws Exception {
        OsiamConnector.Builder authBuilder = new OsiamConnector.Builder(ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("hsimpson").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = authBuilder.build();
        accessToken = oConnector.retrieveAccessToken();
    }
       
}