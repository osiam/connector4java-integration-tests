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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
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
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class SearchUserServiceIT extends AbstractIntegrationTestBase {

    private static final int ITEMS_PER_PAGE = 3;
    private static final int STARTINDEX_SECOND_PAGE = 4;
    private SCIMSearchResult<User> queryResult;
    private static final String HASHED_PASSWORD = "cbae73fac0893291c4792ef19d158a589402288b35cb18fb8406e951b9d95f6b8b06a3526ffebe96ae0d91c04ae615a7fe2af362763db386ccbf3b55c29ae800";

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
    public void search_for_user_by_username_with_query_string_works() {
        String userName = "bjensen";
        Query query = new QueryBuilder().filter("userName eq \"" + userName + "\"").build();

        SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(1L)));
        User transmittedUser = result.getResources().get(0);
        assertThat(transmittedUser.getUserName(), is(equalTo(userName)));
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
    public void search_for_user_by_nonexistent_username_with_query_string_fails() {
        Query query = new QueryBuilder().filter("userName eq \"" + INVALID_STRING + "\"").build();
        SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);
        assertThat(result.getTotalResults(), is(equalTo(0L)));
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_username.xml")
    public void search_for_all_users_ordered_by_user_name_with_query_builder_works()
            throws UnsupportedEncodingException {
        Query query = new QueryBuilder().ascending("userName").build();
        queryResult = oConnector.searchUsers(query, accessToken);

        ArrayList<String> sortedUserNames = new ArrayList<>();
        sortedUserNames.add("bjensen");
        sortedUserNames.add("jcambell");
        sortedUserNames.add("adavies");
        sortedUserNames.add("cmiller");
        sortedUserNames.add("dcooper");
        sortedUserNames.add("epalmer");
        sortedUserNames.add("gparker");
        sortedUserNames.add("hsimpson");
        sortedUserNames.add("kmorris");
        sortedUserNames.add("ewilley");
        sortedUserNames.add("marissa");
        Collections.sort(sortedUserNames);

        assertEquals(sortedUserNames.size(), queryResult.getTotalResults());
        int count = 0;
        for (User actUser : queryResult.getResources()) {
            assertEquals(sortedUserNames.get(count++), actUser.getUserName());
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_email.xml")
    public void search_for_user_by_emails_value_with_query_string_works() {
        String email = "bjensen@example.com";
        Query query = new QueryBuilder().filter("emails.value eq \"" + email + "\"").build();

        SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(1L)));
        User transmittedUser = result.getResources().get(0);
        assertThat(transmittedUser.getEmails().get(0).getValue(), is(equalTo(email)));
    }
    
    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_email.xml")
    public void search_with_double_quotes_less_value_returns_correct_exception_message() {
        String email = "bjensen@example.com";
        Query query = new QueryBuilder().filter("emails.value eq " + email + "").build();

        try{
            oConnector.searchUsers(query, accessToken);
            fail("expected exception");
        }catch(ConflictException e){
            assertTrue(e.getMessage().contains("Please make sure that all values are surrounded by double quotes"));
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_last_modified.xml")
    public void search_for_all_users_ordered_by_last_modified_with_query_builder_works()
            throws UnsupportedEncodingException {
        Query query = new QueryBuilder().ascending("meta.lastModified").build();
        SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);

        ArrayList<String> sortedUserNames = new ArrayList<>();
        sortedUserNames.add("marissa");
        sortedUserNames.add("adavies");
        sortedUserNames.add("bjensen");
        sortedUserNames.add("cmiller");
        sortedUserNames.add("dcooper");
        sortedUserNames.add("epalmer");

        assertThat(result.getTotalResults(), is(equalTo((long) sortedUserNames.size())));

        int count = 0;
        for (User currentUser : result.getResources()) {
            Assert.assertThat(currentUser.getUserName(), is(equalTo(sortedUserNames.get(count++))));
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_complex_query.xml")
    public void search_for_user_by_complex_query() {
        Query query = new QueryBuilder().filter("userName eq \"user1\" and name.formatted eq \"formatted1\""
                + " and emails eq \"email1@other.com\" and extension.stringValue eq \"Hello 1\"").build();
        queryResult = oConnector.searchUsers(query, accessToken);
        assertThat(queryResult.getTotalResults(), is(equalTo(1L)));
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_user_with_multiple_fields() throws UnsupportedEncodingException {
        Query query = new QueryBuilder().filter(
                "title eq \"Dr.\" and nickName eq \"Barbara\" and displayName eq \"BarbaraJ.\"").build();
        whenSearchedIsDoneByQuery(query);
        assertThatQueryResultContainsOnlyValidUser();
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_3_users_by_username_using_and() {
        String user01 = "cmiller";
        String user02 = "hsimpson";
        String user03 = "kmorris";
        Query query = new QueryBuilder().filter("userName eq \"" + user01 + "\" and userName eq \"" + user02
                + "\" and userName eq \"" + user03 + "\"").build();
        whenSearchIsDoneByString(query);
        assertThatQueryResultDoesNotContainValidUsers();
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_3_users_by_username_using_or() {
        String user01 = "cmiller";
        String user02 = "hsimpson";
        String user03 = "kmorris";
        Query query = new QueryBuilder().filter("userName eq \"" + user01 + "\" or userName eq \"" + user02
                + "\" or userName eq \"" + user03 + "\"").build();
        whenSearchIsDoneByString(query);
        assertThatQueryResultContainsUser(user01);
        assertThatQueryResultContainsUser(user02);
        assertThatQueryResultContainsUser(user03);
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_with_braces() throws Exception {

        String filter = "meta.created gt \"2000-05-23T13:12:45.672Z\" and "
                + "(userName eq \"marissa\" or userName eq \"hsimpson\")";
        Query query = new QueryBuilder().filter(filter).build();

        queryResult = oConnector.searchUsers(query, accessToken);
        assertEquals(2, queryResult.getTotalResults());
        assertThatQueryResultContainsUser("marissa");
        assertThatQueryResultContainsUser("hsimpson");
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void nextPage_scrolls_forward() throws UnsupportedEncodingException {
        Query builder = new QueryBuilder().count(ITEMS_PER_PAGE).build();
        Query query = builder.nextPage();
        whenSearchedIsDoneByQuery(query);
        assertEquals(STARTINDEX_SECOND_PAGE, queryResult.getStartIndex());
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void prevPage_scrolls_backward() throws UnsupportedEncodingException {
        Query oldQuery = new QueryBuilder().count(ITEMS_PER_PAGE).startIndex(
                STARTINDEX_SECOND_PAGE).build();
        Query query = oldQuery.previousPage();
        whenSearchedIsDoneByQuery(query);
        assertEquals(1, queryResult.getStartIndex());
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed_over_100_user.xml")
    public void get_all_user_if_over_hundert_user_exists() {
        List<User> allUsers = oConnector.getAllUsers(accessToken);
        assertEquals(111, allUsers.size());
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed_groups.xml")
    public void searching_for_users_belonging_to_a_specific_group_by_displayname() {
        Query query = new QueryBuilder().filter("groups.display eq \"test_group01\"").build();
        Set<String> expectedUserNames = new HashSet<>(Arrays.asList("bjensen", "jcambell", "adavies"));

        queryResult = oConnector.searchUsers(query, accessToken);

        assertThat(queryResult.getResources().size(), is(equalTo(expectedUserNames.size())));
        for (User user : queryResult.getResources()) {
            assertThat(expectedUserNames, hasItem(user.getUserName()));
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed_groups.xml")
    public void searching_for_users_belonging_to_multiple_groups_by_displayname() {
        Query query = new QueryBuilder().filter(
                "groups.display eq \"test_group01\" and groups.display eq \"test_group02\"").build();

        queryResult = oConnector.searchUsers(query, accessToken);

        Set<String> expectedUserNames = new HashSet<>(Arrays.asList("bjensen", "adavies"));
        assertThat(queryResult.getResources().size(), is(equalTo(expectedUserNames.size())));
        for (User user : queryResult.getResources()) {
            assertThat(expectedUserNames, hasItem(user.getUserName()));
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed_groups.xml")
    public void searching_for_users_belonging_to_a_specific_group_by_id() {
        Query query = new QueryBuilder().filter("groups eq \"69e1a5dc-89be-4343-976c-b5541af249f4\"").build();
        Set<String> expectedUserNames = new HashSet<>(Arrays.asList("bjensen", "jcambell", "adavies"));

        queryResult = oConnector.searchUsers(query, accessToken);

        assertThat(queryResult.getResources().size(), is(equalTo(expectedUserNames.size())));
        for (User user : queryResult.getResources()) {
            assertThat(expectedUserNames, hasItem(user.getUserName()));
        }
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_user_by_Password_with_query_string_fails() {
        Query query = new QueryBuilder().filter("password eq \"" + HASHED_PASSWORD + "\"").build();
        queryResult = oConnector.searchUsers(query, accessToken);
        User user = queryResult.getResources().get(0);
        assertThat(user.getUserName(), is("marissa"));
    }

    @Test(expected = ConflictException.class)
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_user_by_non_exisitng_field_with_query_string_fails() {
        Query query = new QueryBuilder().filter(INVALID_STRING + " eq \"" + INVALID_STRING + "\"").build();
        oConnector.searchUsers(query, accessToken);
        fail("Exception should be thrown");
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed.xml")
    public void search_for_user_with_just_some_fields() {
        Query query = new QueryBuilder().filter("username eq \"gparker\"")
                .attributes("userName, emails, nickName").build();
        SCIMSearchResult<User> searchResults = oConnector.searchUsers(query, accessToken);

        assertThat(searchResults.getTotalResults(), is(1L));
        User user = searchResults.getResources().get(0);
        assertThat(user.getUserName(), notNullValue());
        assertThat(user.getName(), nullValue());
    }

    @Test
    @DatabaseSetup("/database_seeds/SearchUserServiceIT/user_by_complex_query.xml")
    public void search_for_user_with_some_attributes() throws UnsupportedEncodingException {
        Query queryString = new QueryBuilder()
                .filter("meta.created gt \"2011-10-10T00:00:00.000\" and userName eq \"user1\"")
                .attributes("userName, displayName, extension").build();
        List<User> users = oConnector.searchUsers(queryString, accessToken).getResources();
        assertThat(users.size(), is(1));
        User user = users.get(0);
        assertThat(user.getEmails().size(), is(0));
        assertThat(user.getUserName(), is("user1"));
        assertThat(user.getDisplayName(), is("user1DisplayName"));
        assertThat(user.getExtensions().size(), is(1));
    }

    private void create100NewUser() {
        for (int count = 0; count < 100; count++) {
            User user = new User.Builder("user" + count).build();
            oConnector.createUser(user, accessToken);
        }
    }

    private void assertThatQueryResultContainsUser(String userName) {
        assertTrue(queryResult != null);
        for (User actUser : queryResult.getResources()) {
            if (actUser.getUserName().equals(userName)) {
                return; // OK
            }
        }
        fail("User " + userName + " could not be found.");
    }

    private void assertThatQueryResultContainsOnlyValidUser() {
        assertTrue(queryResult != null);
        assertEquals(1, queryResult.getTotalResults());
        assertThatQueryResultContainsValidUser();
    }

    private void assertThatQueryResultDoesNotContainValidUsers() {
        assertTrue(queryResult != null);
        assertEquals(0, queryResult.getTotalResults());
    }

    private void whenSearchIsDoneByString(Query query) {
        queryResult = oConnector.searchUsers(query, accessToken);
    }

    private void whenSearchedIsDoneByQuery(Query query) {
        queryResult = oConnector.searchUsers(query, accessToken);
    }

    private void assertThatQueryResultContainsValidUser() {
        assertTrue(queryResult != null);
        for (User actUser : queryResult.getResources()) {
            if (actUser.getId().equals(VALID_USER_ID)) {
                return; // OK
            }
        }
        fail("Valid user could not be found.");
    }

}
