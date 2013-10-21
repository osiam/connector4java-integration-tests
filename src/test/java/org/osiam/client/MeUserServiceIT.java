package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
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
@Ignore("/User/me is no longer available and '/me' is not yet supported by connector")
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
    @Ignore("/User/me is no longer available and '/me' is not yet supported by connector")
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

        List<MultiValuedAttribute> emails = deserializedUser.getEmails();
        assertEquals(1, emails.size());
        MultiValuedAttribute email = emails.get(0);

        assertEquals("bjensen@example.com", email.getValue().toString());
        assertEquals("work", email.getType());
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

        List<MultiValuedAttribute> emails = sortEmails(deserializedUser.getEmails());
        assertEquals(2, emails.size());

        MultiValuedAttribute email1 = emails.get(0);
        assertEquals("hsimpson@atom-example.com", email1.getValue().toString());
        assertEquals("work", email1.getType());

        MultiValuedAttribute email2 = emails.get(1);
        assertEquals("hsimpson@phome-example.com", email2.getValue().toString());
        assertEquals("home", email2.getType());
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
        // Sleeping snake is a test anti-pattern! Timing is not a good idea.
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

    private List<MultiValuedAttribute> sortEmails(List<MultiValuedAttribute> emails) {
        Collections.sort(emails, new Comparator<MultiValuedAttribute>() {
            @Override
            public int compare(MultiValuedAttribute o1, MultiValuedAttribute o2) {
                return o1.getType().compareTo(o2.getType());
            }
        });

        return emails;
    }

    private void givenAnAccessTokenForBJensen() throws Exception {
        OsiamConnector.Builder authBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
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
        OsiamConnector.Builder authBuilder = new OsiamConnector.Builder().
                setAuthServiceEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceEndpoint(RESOURCE_ENDPOINT_ADDRESS).
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