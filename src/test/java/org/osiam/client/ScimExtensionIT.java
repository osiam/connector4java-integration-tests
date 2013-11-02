package org.osiam.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.resources.scim.Extension;
import org.osiam.resources.scim.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;

import static org.hamcrest.CoreMatchers.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
//@DatabaseTearDown(value = "/database_seed_extensions.xml", type = DatabaseOperation.DELETE_ALL)
public class ScimExtensionIT extends AbstractIntegrationTestBase {

    private static final String URN = "extension";
    private static final String EXISTING_USER_UUID = "df7d06b2-b6ee-42b1-8c1b-4bd1176cc8d4";

    private Map<String, String> extensionData;
    private Map<String, String> extensionDataToPatch;
    
    @Autowired
    private DataSource dataSource;
    
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
    @DatabaseSetup(value = "/database_seed_extensions.xml")
    public void retrieving_a_user_with_extension_works() {
        User storedUser = oConnector.getUser(EXISTING_USER_UUID, accessToken);
        
        assertTrue(storedUser.getSchemas().contains(URN));
        Extension storedExtension = storedUser.getExtension(URN);
        for (Map.Entry<String, String> entry : extensionData.entrySet()) {
            assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
        }
    }
    
    @Test
    @DatabaseSetup("/database_seed_extensions_and_multiple_users.xml")
    public void retrieving_multiple_users_with_extension_via_query_works() {
        Query query = new Query.Builder(User.class)
            .setFilter("userName co existing")
            .build();
        
        QueryResult<User> result = oConnector.searchUsers(query, accessToken);
        
        assertThat(result.getResources().size(), is(3));
        for (User storedUser : result.getResources()) {
            assertTrue(storedUser.getSchemas().contains(URN));
            Extension storedExtension = storedUser.getExtension(URN);
            for (Map.Entry<String, String> entry : extensionData.entrySet()) {
                assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
            }
        }
    }
    
    @Test
    @DatabaseSetup(value = "/database_seed_extensions.xml")
    @ExpectedDatabase(value = "/database_expected_extensions.xml", assertionMode = DatabaseAssertionMode.NON_STRICT)
    public void adding_a_user_with_extension_data_to_database_works() {
        Extension extension = new Extension(URN, extensionData);
        User user = new User.Builder("userName")
                .setPassword("password")
                .addExtension(URN, extension)
                .build();

        String uuid = oConnector.createUser(user, accessToken).getId();

        User storedUser = oConnector.getUser(uuid, accessToken);
        assertTrue(storedUser.getSchemas().contains(URN));
        Extension storedExtension = storedUser.getExtension(URN);
        for (Map.Entry<String, String> entry : extensionData.entrySet()) {
            assertEquals(entry.getValue(), storedExtension.getField(entry.getKey()));
        }
    }

    @Test
    @DatabaseSetup(value = "/database_seed_extensions.xml")
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
    @DatabaseSetup(value = "/database_seed_extensions.xml")
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
    
    @Test
    @DatabaseSetup(value = "/database_seed_extensions.xml")
    public void deleting_a_user_also_deletes_her_extension_values() throws SQLException {
        oConnector.deleteUser(EXISTING_USER_UUID, accessToken);
        
        String sql = "SELECT count(*) FROM SCIM_EXTENSION_FIELD_VALUE WHERE USER_INTERNAL_ID = 2";
        ResultSet rs = dataSource.getConnection().createStatement().executeQuery(sql);
        rs.first();
        
        assertThat(rs.getInt(1), is(0));
    }
    
    @Test
    @DatabaseSetup(value = "/database_seed_extensions_with_less_values.xml")
    public void updating_a_user_with_less_extension_data_to_database_works() {
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
