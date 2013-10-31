package org.osiam.client;

import java.util.HashMap;
import java.util.Map;

import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/database_seed_extensions.xml")
//@DatabaseTearDown(value = "/database_seed_extensions.xml", type = DatabaseOperation.DELETE_ALL)
public class ScimExtensionIT extends AbstractIntegrationTestBase {

    private static final String URN = "extension";

    @Test
  //  @ExpectedDatabase(value = "/database_expected_extensions.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void adding_a_user_with_extension_data_to_database_works() {
        // given
        Map<String,String> extensionData = new HashMap<>();
        extensionData.put("gender", "male");
        extensionData.put("size", "1337");
        extensionData.put("birth", "Wed Oct 30 16:54:00 CET 1985");
        extensionData.put("newsletter", "false");
        extensionData.put("married", "false");
        Extension extension = new Extension(URN, extensionData);
        User user = new User.Builder("userName")
                .setPassword("password")
                .addExtension(URN, extension)
                .build();

        //when:
        User userCreated = oConnector.createUser(user, accessToken);

        //then
        userCreated.getUserName().equals("userName");
    }

    @Test
    //  @ExpectedDatabase(value = "/database_expected_extensions.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void updating_a_user_with_extension_data_to_database_works() {
        // given
        User existingUser = oConnector.getUser("df7d06b2-b6ee-42b1-8c1b-4bd1176cc8d4",accessToken);

        Extension extension = existingUser.getExtension(URN);
        extension.setField("gender", "female");

        oConnector.updateUser(existingUser, accessToken);




    }


}
