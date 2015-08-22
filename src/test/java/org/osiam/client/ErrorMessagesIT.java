/*
 * Copyright (C) 2013 tarent AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.UpdateGroup;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class ErrorMessagesIT extends AbstractIntegrationTestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    /**
     * example message: User with id '81b2be7e-9e2c-40ce-af3c-0567ce904166' not found
     */
    @Test
    public void retrieving_non_existing_User_returns_correct_error_message() {
        thrown.expect(NoResultException.class);
        thrown.expectMessage(startsWith("User"));
        thrown.expectMessage(containsString("not found"));

        OSIAM_CONNECTOR.getUser(UUID.randomUUID().toString(), accessToken);
    }

    /**
     * example message: Group with id 'ba3e1fda-dc72-486a-84a7-c6f836dc1607' not found
     */
    @Test
    public void retrieving_non_existing_Group_returns_correct_error_message() {
        thrown.expect(NoResultException.class);
        thrown.expectMessage(startsWith("Group"));
        thrown.expectMessage(containsString("not found"));

        OSIAM_CONNECTOR.getGroup(UUID.randomUUID().toString(), accessToken);
    }

    /**
     * example message: User with id '7c198d82-f03b-4fa8-806c-16b26172bba5' not found
     */
    @Test
    public void retrieving_User_with_Group_id_returns_correct_error_message() {
        thrown.expect(NoResultException.class);
        thrown.expectMessage(startsWith("User"));
        thrown.expectMessage(containsString("not found"));

        OSIAM_CONNECTOR.getUser("7c198d82-f03b-4fa8-806c-16b26172bba5", accessToken);
    }

    /**
     * example message: The attribute userName is mandatory and MUST NOT be null
     */
    @Test
    public void create_user_without_userName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("userName"));
        thrown.expectMessage(containsString("mandatory"));

        User user = new User.Builder().build();
        OSIAM_CONNECTOR.createUser(user, accessToken);
    }

    /**
     * example message: The attribute displayName is mandatory and MUST NOT be null
     */
    @Test
    public void create_group_without_displayName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("displayName"));
        thrown.expectMessage(containsString("mandatory"));

        Group group = new Group.Builder().build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    /**
     * example message: Can't create a user. The username "jcambell" is already taken.
     */
    @Test
    public void create_user_with_existing_userName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingUserName = "jcambell";
        User user = new User.Builder(existingUserName).build();
        OSIAM_CONNECTOR.createUser(user, accessToken);
    }

    /**
     * example message: Can't create a user. The externalId "cmiller" is already taken.
     */
    @Test
    public void create_user_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingExternalId = "cmiller";
        User user = new User.Builder("newUser").setExternalId(existingExternalId).build();
        OSIAM_CONNECTOR.createUser(user, accessToken);
    }

    /**
     * example message: Can't update the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "jcambell" is already taken.
     */
    @Test
    public void update_user_with_existing_userName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingUserName = "jcambell";
        UpdateUser updateUser = new UpdateUser.Builder().updateUserName(existingUserName).build();
        OSIAM_CONNECTOR.updateUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", updateUser, accessToken);
    }

    /**
     * example message: Can't update the group with the id "0cb908cf-81a9-4966-803f-a3eb92968bb4". The displayname
     * "test_group10" is already taken.
     */
    @Test
    public void update_group_with_existing_displayName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("already taken"));

        String existingUserName = "test_group10";
        UpdateGroup updateUser = new UpdateGroup.Builder().updateDisplayName(existingUserName).build();
        OSIAM_CONNECTOR.updateGroup("0cb908cf-81a9-4966-803f-a3eb92968bb4", updateUser, accessToken);
    }

    /**
     * example message: Can't update the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "jcambell" is already taken.
     */
    @Test
    public void update_user_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingExternalId = "cmiller";
        UpdateUser updateUser = new UpdateUser.Builder().updateExternalId(existingExternalId).build();
        OSIAM_CONNECTOR.updateUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", updateUser, accessToken);
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void update_group_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("already taken"));

        String existingExternalId = "cmiller";
        UpdateGroup updateUser = new UpdateGroup.Builder().updateExternalId(existingExternalId).build();
        OSIAM_CONNECTOR.updateGroup("69e1a5dc-89be-4343-976c-b5541af249f4", updateUser, accessToken);
    }

    /**
     * example message: Can't replace the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "cmiller" is already taken.
     */
    @Test
    public void replace_user_with_existing_username_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingUsername = "cmiller";
        User user = new User.Builder(existingUsername).build();
        OSIAM_CONNECTOR.replaceUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", user, accessToken);
    }

    /**
     * example message: Can't replace the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void replace_user_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("user"));
        thrown.expectMessage(containsString("already taken"));

        String existingExternalId = "cmiller";
        User user = new User.Builder("newUser").setExternalId(existingExternalId).build();
        OSIAM_CONNECTOR.replaceUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", user, accessToken);
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The displayname
     * "test_group02" is already taken.
     */
    @Test
    public void replace_group_with_existing_displayname_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("already taken"));

        String existingDisplayName = "test_group02";
        Group group = new Group.Builder(existingDisplayName).setId("69e1a5dc-89be-4343-976c-b5541af249f4").build();
        OSIAM_CONNECTOR.replaceGroup("69e1a5dc-89be-4343-976c-b5541af249f4", group, accessToken);
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void replace_group_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("already taken"));

        String existingExternalId = "cmiller";
        Group group = new Group.Builder("newGroup").setExternalId(existingExternalId)
                .setId("69e1a5dc-89be-4343-976c-b5541af249f4").build();
        OSIAM_CONNECTOR.replaceGroup("69e1a5dc-89be-4343-976c-b5541af249f4", group, accessToken);
    }

    /**
     * example message: Can't create a group. The displayname "test_group02" is already taken
     */
    @Test
    public void create_group_with_existing_displayName_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("displayname"));
        thrown.expectMessage(containsString("already taken"));

        String existingGroupName = "test_group02";
        Group group = new Group.Builder(existingGroupName).build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    /**
     * example message: Can't create a group. The externalId "cmiller" is already taken.
     */
    @Test
    public void create_group_with_existing_external_id_returns_correct_error_message() {
        thrown.expect(ConflictException.class);
        thrown.expectMessage(containsString("group"));
        thrown.expectMessage(containsString("externalId"));
        thrown.expectMessage(containsString("already taken"));

        String existingGroupName = "newGroup";
        Group group = new Group.Builder(existingGroupName).setExternalId("cmiller").build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    /**
     * example message: Bad credentials
     */
    @Test
    public void login_with_wrong_client_credentials() {
        thrown.expect(UnauthorizedException.class);
        thrown.expectMessage(containsString("Bad credentials"));

        final OsiamConnector connectorWithWrongSecret = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId("example-client").
                setClientSecret("wrongsecret")
                .build();
        connectorWithWrongSecret.retrieveAccessToken();
    }
}
