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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.SCIMSearchResult;
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
public class EditUserServiceIT extends AbstractIntegrationTestBase {

    private static final String ID_EXISTING_USER = "7d33bcbe-a54c-43d8-867e-f6146164941e";
    private static final String IRRELEVANT = "irrelevant";
    private static final String USER_NAME_EXISTING_USER = "hsimpson";

    private String NEW_ID = UUID.randomUUID().toString();
    private User newUser;
    private User returnedUser;
    private User dbUser;
    private String validId = null;
    private Query query;

    @Before
    public void setup() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    @Test(expected = ConflictException.class)
    public void create_user_with_no_username_raises_exception() {
        initializeUserWithNoUserName();
        createUser();
        fail("Exception expected");
    }

    @Test(expected = ConflictException.class)
    public void create_user_with_existing_username_raises_exception() {
        initializeUserWithExistingUserName();
        createUser();
        fail("Exception expected");
    }

    @Test
    public void create_simple_User() {
        initializeSimpleUser();
        createUser();
        returnUserHasValidId();
        loadUser(returnedUser.getId());
        assertEquals(newUser.getUserName(), dbUser.getUserName());
    }

    @Test
    public void create_user_with_existing_id() {
        initializeSimpleUserWithID(ID_EXISTING_USER);
        createUser();
        loadUser(ID_EXISTING_USER);
        assertEquals(USER_NAME_EXISTING_USER, dbUser.getUserName());
    }

    @Test
    public void given_id_to_new_user_has_changed_after_saving() {
        initializeSimpleUserWithID(NEW_ID);
        createUser();
        assertNotSame(NEW_ID, returnedUser.getId());
    }

    @Test
    public void created_user_can_be_found() {
        initialQueryToSearchUser();
        loadSingleUserByQuery();
        assertNull(dbUser);

        initializeSimpleUser();
        createUser();
        loadSingleUserByQuery();
        assertNotNull(dbUser);
        assertNotSame(IRRELEVANT, dbUser.getUserName());
    }

    @Test
    public void id_return_user_same_as_new_loaded_id() {
        initializeSimpleUserWithID(NEW_ID);
        createUser();
        initialQueryToSearchUser();
        loadSingleUserByQuery();
        assertNotNull(dbUser);
        assertEquals(returnedUser.getId(), dbUser.getId());
    }

    @Test
    public void create_complete_user() {
        try {
            buildCompleteUser();
            createUser();
            initialQueryToSearchUser();
            loadSingleUserByQuery();
            assertNotNull(dbUser);
            assertEquals(returnedUser.getId(), dbUser.getId());
            assertEqualsUser(newUser, dbUser);
        } finally {
            if (returnedUser != null) {
                OSIAM_CONNECTOR.deleteUser(returnedUser.getId(), accessToken);
            }
        }
    }

    @Test
    public void user_is_deleted() throws Exception {
        givenAValidUserIDForDeletion();
        whenUserIsDeleted();
        assertThatUserIsRemoveFromServer();
    }

    @Test(expected = NoResultException.class)
    public void delete_user_two_times() throws Exception {
        givenAValidUserIDForDeletion();
        whenUserIsDeleted();
        assertThatUserIsRemoveFromServer();
        whenUserIsDeleted();
        fail();
    }

    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception()
            throws Exception {
        givenAValidUserIDForDeletion();
        givenAnInvalidAccessToken();
        whenUserIsDeleted();
        fail();
    }

    private void initializeUserWithNoUserName() {
        newUser = new User.Builder().build();
    }

    private void initializeSimpleUser() {
        newUser = new User.Builder(IRRELEVANT).build();
    }

    private void initializeSimpleUserWithID(String id) {
        newUser = new User.Builder(IRRELEVANT).setId(id).build();
    }

    private void initializeUserWithExistingUserName() {
        newUser = new User.Builder(USER_NAME_EXISTING_USER).build();
    }

    private void returnUserHasValidId() {
        assertTrue(returnedUser.getId().length() > 0);
    }

    private void loadUser(String id) {
        dbUser = OSIAM_CONNECTOR.getUser(id, accessToken);
    }

    private void loadSingleUserByQuery() {
        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query,
                accessToken);
        if (result.getResources().size() == 0) {
            dbUser = null;
        } else if (result.getResources().size() == 1) {
            dbUser = result.getResources().get(0);
        } else {
            fail("No or one user should be found");
        }
    }

    private void createUser() {
        returnedUser = OSIAM_CONNECTOR.createUser(newUser, accessToken);
    }

    private void initialQueryToSearchUser() {
        query = new QueryBuilder()
                .filter("userName eq \"" + IRRELEVANT + "\"").build();
    }

    private void buildCompleteUser() {
        Address address = new Address.Builder()
                .setStreetAddress("Example Street 22").setCountry("Germany")
                .setFormatted("Complete Address").setLocality("de")
                .setPostalCode("111111").setRegion("Berlin").build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        Email email01 = new Email.Builder().setValue("example@example.de")
                .setPrimary(true).setType(Email.Type.WORK).build();
        Email email02 = new Email.Builder().setValue("example02@example.de")
                .setPrimary(false).setType(Email.Type.HOME).build();
        List<Email> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);

        Name name = new Name.Builder().setFamilyName("familyName")
                .setGivenName("vorName").setMiddleName("middle")
                .setFormatted("complete Name").setHonorificPrefix("HPre")
                .setHonorificSuffix("HSu").build();

        newUser = new User.Builder(IRRELEVANT).setPassword("password")
                .setActive(true).addAddresses(addresses).setLocale("de")
                .setName(name).setNickName("aNickname").setTitle("Dr.")
                .addEmails(emails).build();
    }

    private void assertEqualsUser(User expected, User actual) {
        assertEquals(expected.getUserName(), actual.getUserName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEqualsAddresses(expected.getAddresses(), actual.getAddresses());
        assertEquals(expected.getLocale(), actual.getLocale());
        assertEqualsName(expected.getName(), actual.getName());
        assertEquals(expected.getNickName(), actual.getNickName());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEqualsEmailAttribute(expected.getEmails(), actual.getEmails());

    }

    private void assertEqualsEmailAttribute(
            List<Email> expectedMultiValuedAttributes,
            List<Email> actualMultiValuedAttributes) {
        if ((expectedMultiValuedAttributes == null || expectedMultiValuedAttributes
                .size() == 0)
                && (actualMultiValuedAttributes == null || actualMultiValuedAttributes
                        .size() == 0)) {
            return;
        }
        assertEquals(expectedMultiValuedAttributes.size(),
                actualMultiValuedAttributes.size());
        for (int count = 0; count < expectedMultiValuedAttributes.size(); count++) {
            Email expectedEmail = expectedMultiValuedAttributes.get(count);
            Email actualEmail = getMultiAttributeWithValue(
                    actualMultiValuedAttributes, expectedEmail.getValue());
            if (actualEmail == null) {
                fail("MultiValueAttribute " + expectedEmail.getValue()
                        + " could not be found");
            }

            assertEquals(expectedEmail.getDisplay(), actualEmail.getDisplay());
            assertEquals(expectedEmail.getOperation(),
                    actualEmail.getOperation());
            assertEquals(expectedEmail.getType(), actualEmail.getType());
            assertEquals(expectedEmail.getValue(), actualEmail.getValue());
        }
    }

    private Email getMultiAttributeWithValue(List<Email> multiValuedAttributes,
            String expectedValue) {
        Email email = null;
        for (Email actAttribute : multiValuedAttributes) {
            if (actAttribute.getValue().equals(expectedValue)) {
                email = actAttribute;
                break;
            }
        }
        return email;
    }

    private void assertEqualsName(Name expectedName, Name actualName) {
        assertEquals(expectedName.getFamilyName(), actualName.getFamilyName());
        assertEquals(expectedName.getFormatted(), actualName.getFormatted());
        assertEquals(expectedName.getGivenName(), actualName.getGivenName());
        assertEquals(expectedName.getHonorificPrefix(),
                actualName.getHonorificPrefix());
        assertEquals(expectedName.getHonorificSuffix(),
                actualName.getHonorificSuffix());
        assertEquals(expectedName.getMiddleName(), actualName.getMiddleName());
    }

    private void assertEqualsAddresses(List<Address> expectedAddresses,
            List<Address> actualAddresses) {
        assertEquals(expectedAddresses.size(), actualAddresses.size());
        for (int count = 0; count < expectedAddresses.size(); count++) {
            Address expectedAddress = expectedAddresses.get(count);
            Address actualAddress = actualAddresses.get(count);

            assertEquals(expectedAddress.getCountry(),
                    actualAddress.getCountry());
            assertEquals(expectedAddress.getFormatted(),
                    actualAddress.getFormatted());
            assertEquals(expectedAddress.getLocality(),
                    actualAddress.getLocality());
            assertEquals(expectedAddress.getPostalCode(),
                    actualAddress.getPostalCode());
            assertEquals(expectedAddress.getRegion(), actualAddress.getRegion());
            assertEquals(expectedAddress.getStreetAddress(),
                    actualAddress.getStreetAddress());
        }
    }

    private void whenUserIsDeleted() {
        OSIAM_CONNECTOR.deleteUser(validId, accessToken);
    }

    private void givenAValidUserIDForDeletion() throws Exception {
        validId = DELETE_USER_ID;
    }

    private void assertThatUserIsRemoveFromServer() {
        try {
            OSIAM_CONNECTOR.getUser(validId, accessToken);
        } catch (NoResultException e) {
            return;
        } catch (Exception e) {
            fail(Arrays.toString(e.getStackTrace()));
        }
        fail();
    }
}
