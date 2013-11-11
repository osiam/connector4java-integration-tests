package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.update.UpdateUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed_activation.xml")
@DatabaseTearDown(value = "/database_seed_activation.xml", type = DatabaseOperation.DELETE_ALL)
public class UserActivationLoginIT extends AbstractIntegrationTestBase{

    @Test
    public void should_only_be_possible_to_login_with_a_user_if_he_is_activated() {
        //given: trying to get an access token with deactivated user should fail
        AccessToken accessTokenForTest = null;
        try {
            OsiamConnector connector = getAccessTokenForUser("hsimpson", "koala");
            accessTokenForTest = connector.retrieveAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert accessTokenForTest == null;

        //when: activating an already existing user
        oConnector.updateUser("7d33bcbe-a54c-43d8-867e-f6146164941e",
                new UpdateUser.Builder().updateActive(true).build(), accessToken);

        //then: getting an access token with the activated user should be possible
        OsiamConnector connector = getAccessTokenForUser("hsimpson", "koala");
        accessTokenForTest = connector.retrieveAccessToken();
        assert accessTokenForTest != null;
    }
}