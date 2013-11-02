package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed_extensions.xml")
//@DatabaseTearDown(value = "/database_seed_extensions.xml", type = DatabaseOperation.DELETE_ALL)
public class ScimExtensionIT extends AbstractIntegrationTestBase {

    private static final String URN = "extension";
    private static final String EXISTING_USER_UUID = "df7d06b2-b6ee-42b1-8c1b-4bd1176cc8d4";

    private Map<String, String> extensionData;
    private Map<String, String> extensionDataToPatch;

    @Before
    public void setUp() {
        extensionData = new HashMap<>();
        extensionData.put("gender", "male");
        extensionData.put("size", "1337");
        extensionData.put("birth", "Wed Oct 30 16:54:00 CET 1985");
        extensionData.put("newsletter", "false");
        extensionData.put("married", "false");
        
        extensionDataToPatch = new HashMap<>();
        extensionDataToPatch.put("gender", "female");
        extensionDataToPatch.put("size", "0000");
        extensionDataToPatch.put("birth", "Wed Nov 30 18:34:00 CET 1979");
        extensionDataToPatch.put("newsletter", "true");
        extensionDataToPatch.put("married", "true");
    }

    @Test
    @ExpectedDatabase(value = "/database_expected_extensions.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void adding_a_user_with_extension_data_to_database_works() {
        Extension extension = new Extension(URN, extensionData);
        User user = new User.Builder("userName")
                .setPassword("password")
                .addExtension(URN, extension)
                .build();

        String uuid = oConnector.createUser(user, accessToken).getId();

        User storedUser = oConnector.getUser(uuid, accessToken);
        Extension storedExtension = storedUser.getExtension(URN);
        for (Map.Entry<String, String> entry : extensionData.entrySet()) {
            assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
        }
    }

    @Test
    public void replacing_a_user_with_extension_data_to_database_works() {
        User existingUser = oConnector.getUser(EXISTING_USER_UUID, accessToken);
        extensionData.put("gender", "female");
        Extension extension = existingUser.getExtension(URN);
        extension.setField("gender", "female");

        oConnector.replaceUser(existingUser, accessToken);

        User storedUser = oConnector.getUser(EXISTING_USER_UUID, accessToken);
        Extension storedExtension = storedUser.getExtension(URN);
        for (Map.Entry<String, String> entry : extensionData.entrySet()) {
            assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
        }
    }

    @Test
    public void updating_a_user_with_extension_data_to_database_works() {
        Extension extension = new Extension(URN, extensionDataToPatch);
        User patchUser = new User.Builder()
                .addExtension(URN, extension)
                .build();

        oConnector.updateUser(EXISTING_USER_UUID, patchUser, accessToken);

        User storedUser = oConnector.getUser(EXISTING_USER_UUID, accessToken);
        Extension storedExtension = storedUser.getExtension(URN);
        for (Map.Entry<String, String> entry : extensionDataToPatch.entrySet()) {
            assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
        }
    }
}
