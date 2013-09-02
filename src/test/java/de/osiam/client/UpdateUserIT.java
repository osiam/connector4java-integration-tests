package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.UUID;

import static junit.framework.Assert.assertNotSame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class UpdateUserIT extends AbstractIntegrationTestBase{

    private UUID VALID_UUID = null;
    private UUID ID_EXISITNG_USER = UUID.fromString("7d33bcbe-a54c-43d8-867e-f6146164941e");
    private UpdateUser UPDATE_USER;
    private User RETURN_USER;
    private User ORIGINAL_USER;
    private String IRRELEVANT = "Irrelevant";

    @Test
    @Ignore
    public void Test01(){
        getOriginalUser();
        createUpdateUserWithAlleFields();
        updateUser();
        assertNotSame(ORIGINAL_USER.getUserName(), RETURN_USER.getUserName());
    }

    public void getOriginalUser(){
        ORIGINAL_USER = oConnector.getUser(ID_EXISITNG_USER, accessToken);
    }

    private void createUpdateUserWithAlleFields(){
        UPDATE_USER = new UpdateUser.Builder(IRRELEVANT).build();
    }

    private void updateUser(){
       RETURN_USER = oConnector.updateUser(ID_EXISITNG_USER, UPDATE_USER, accessToken);
    }


}
