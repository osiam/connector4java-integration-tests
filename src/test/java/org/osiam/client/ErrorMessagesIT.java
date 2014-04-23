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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.UpdateGroup;
import org.osiam.resources.scim.UpdateUser;
import org.osiam.resources.scim.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class ErrorMessagesIT extends AbstractIntegrationTestBase {

    /**
     * example message: User with id '81b2be7e-9e2c-40ce-af3c-0567ce904166' not found
     */
    @Test
    public void retrieving_non_existing_User_returns_correct_error_message() {
        try {
            oConnector.getUser(UUID.randomUUID().toString(), accessToken);
            fail("expected exception");
        } catch (NoResultException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("User"));
            assertTrue(errorMessage.contains("not found"));
        }
    }

    /**
     * example message: Group with id 'ba3e1fda-dc72-486a-84a7-c6f836dc1607' not found
     */
    @Test
    public void retrieving_non_existing_Group_returns_correct_error_message() {
        try {
            oConnector.getGroup(UUID.randomUUID().toString(), accessToken);
            fail("expected exception");
        } catch (NoResultException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("Group"));
            assertTrue(errorMessage.contains("not found"));
        }
    }

    /**
     * example message: User with id '7c198d82-f03b-4fa8-806c-16b26172bba5' not found
     */
    @Test
    public void retrieving_User_with_Group_id_returns_correct_error_message() {
        try {
            oConnector.getUser("7c198d82-f03b-4fa8-806c-16b26172bba5", accessToken);
            fail("expected exception");
        } catch (NoResultException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("User"));
            assertTrue(errorMessage.contains("not found"));
        }
    }

    /**
     * example message: The attribute userName is mandatory and MUST NOT be null
     */
    @Test
    public void create_user_without_userName_returns_correct_error_message() {
        User user = new User.Builder().build();
        try {
            oConnector.createUser(user, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("userName"));
            assertTrue(errorMessage.contains("mandatory"));
        }
    }

    /**
     * example message: The attribute displayName is mandatory and MUST NOT be null
     */
    @Test
    public void create_group_without_displayName_returns_correct_error_message() {
        Group group = new Group.Builder().build();
        try {
            oConnector.createGroup(group, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("displayName"));
            assertTrue(errorMessage.contains("mandatory"));
        }
    }

    /**
     * example message: Can't create a user. The username "jcambell" is already taken.
     */
    @Test
    public void create_user_with_existing_userName_returns_correct_error_message() {
        String existingUserName = "jcambell";
        User user = new User.Builder(existingUserName).build();
        try {
            oConnector.createUser(user, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user"));
            assertTrue(errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't create a user. The externalId "cmiller" is already taken.
     */
    @Test
    public void create_user_with_existing_external_id_returns_correct_error_message() {
        String existingExternalId = "cmiller";
        User user = new User.Builder("newUser").setExternalId(existingExternalId).build();
        try {
            oConnector.createUser(user, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "jcambell" is already taken.
     */
    @Test
    public void update_user_with_existing_userName_returns_correct_error_message() {
        String existingUserName = "jcambell";
        UpdateUser updateUser = new UpdateUser.Builder().updateUserName(existingUserName).build();
        try {
            oConnector.updateUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", updateUser, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the group with the id "0cb908cf-81a9-4966-803f-a3eb92968bb4". The displayname
     * "test_group10" is already taken.
     */
    @Test
    public void update_group_with_existing_displayName_returns_correct_error_message() {
        String existingUserName = "test_group10";
        UpdateGroup updateUser = new UpdateGroup.Builder().updateDisplayName(existingUserName).build();
        try {
            oConnector.updateGroup("0cb908cf-81a9-4966-803f-a3eb92968bb4", updateUser, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "jcambell" is already taken.
     */
    @Test
    public void update_user_with_existing_external_id_returns_correct_error_message() {
        String existingExternalId = "cmiller";
        UpdateUser updateUser = new UpdateUser.Builder().updateExternalId(existingExternalId).build();
        try {
            oConnector.updateUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", updateUser, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void update_group_with_existing_external_id_returns_correct_error_message() {
        String existingExternalId = "cmiller";
        UpdateGroup updateUser = new UpdateGroup.Builder().updateExternalId(existingExternalId).build();
        try {
            oConnector.updateGroup("69e1a5dc-89be-4343-976c-b5541af249f4", updateUser, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't replace the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The username
     * "cmiller" is already taken.
     */
    @Test
    public void replace_user_with_existing_username_returns_correct_error_message() {
        String existingUsername = "cmiller";
        User user = new User.Builder(existingUsername)
                .build();
        try {
            oConnector.replaceUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", user, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't replace the user with the id "aba67300-74f1-4e51-a68a-0a6c5c45b79c". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void replace_user_with_existing_external_id_returns_correct_error_message() {
        String existingExternalId = "cmiller";
        User user = new User.Builder("newUser").setExternalId(existingExternalId)
                .build();
        try {
            oConnector.replaceUser("aba67300-74f1-4e51-a68a-0a6c5c45b79c", user, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("user") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The displayname
     * "test_group02" is already taken.
     */
    @Test
    public void replace_group_with_existing_displayname_returns_correct_error_message() {
        String existingDisplayName = "test_group02";
        Group group = new Group.Builder(existingDisplayName)
                .setId("69e1a5dc-89be-4343-976c-b5541af249f4").build();
        try {
            oConnector.replaceGroup("69e1a5dc-89be-4343-976c-b5541af249f4", group, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't update the group with the id "69e1a5dc-89be-4343-976c-b5541af249f4". The externalId
     * "cmiller" is already taken.
     */
    @Test
    public void replace_group_with_existing_external_id_returns_correct_error_message() {
        String existingExternalId = "cmiller";
        Group group = new Group.Builder("newGroupr").setExternalId(existingExternalId)
                .setId("69e1a5dc-89be-4343-976c-b5541af249f4").build();
        try {
            oConnector.replaceGroup("69e1a5dc-89be-4343-976c-b5541af249f4", group, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group") && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't create a group. The displayname "test_group02" is already taken
     */
    @Test
    public void create_group_with_existing_displayName_returns_correct_error_message() {
        String existingGroupName = "test_group02";
        Group group = new Group.Builder(existingGroupName).build();
        try {
            oConnector.createGroup(group, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group") && errorMessage.contains("displayname")
                    && errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Can't create a group. The externalId "cmiller" is already taken.
     */
    @Test
    public void create_group_with_existing_external_id_returns_correct_error_message() {
        String existingGroupName = "newGroup";
        Group group = new Group.Builder(existingGroupName).setExternalId("cmiller").build();
        try {
            oConnector.createGroup(group, accessToken);
            fail("expected exception");
        } catch (ConflictException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("group"));
            assertTrue(errorMessage.contains("externalId"));
            assertTrue(errorMessage.contains("already taken"));
        }
    }

    /**
     * example message: Bad credentials
     */
    @Test
    public void login_with_wrong_client_credentials() {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder().
                setAuthServerEndpoint(AUTH_ENDPOINT_ADDRESS).
                setResourceServerEndpoint(RESOURCE_ENDPOINT_ADDRESS).
                setClientId("example-client").
                setClientSecret("wrongsecret").
                setGrantType(GrantType.CLIENT_CREDENTIALS).
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        try {
            oConnector.retrieveAccessToken();
        } catch (UnauthorizedException e) {
            String errorMessage = e.getMessage();
            printOutErrorMessage(errorMessage);
            assertTrue(errorMessage.contains("Bad credentials"));
        }
    }

    private void printOutErrorMessage(String errorMessage) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        String methodName = ste[2].getMethodName();
        System.out.println("The error message for " + methodName + " is: " + errorMessage);
    }
}

