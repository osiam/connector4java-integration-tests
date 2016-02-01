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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.BadRequestException;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.SCIMSearchResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup(value = "/database_seeds/EditGroupServiceIT/groups.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class EditGroupServiceIT extends AbstractIntegrationTestBase {

    private static final String IRRELEVANT = "irrelevant";
    private String newId;
    private Group group;

    @Before
    public void setUp() {
        group = null;
        newId = UUID.randomUUID().toString();
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    @Test(expected = BadRequestException.class)
    public void create_group_without_displayName_raises_exception() {
        group = new Group.Builder().build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    @Test(expected = BadRequestException.class)
    public void create_group_with_empty_displayName_raises_exception() {
        group = new Group.Builder("").build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    @Test(expected = ConflictException.class)
    public void create_group_with_existing_displayName_raises_exception() {
        String existingGroupName = "parent_group";
        group = new Group.Builder(existingGroupName).build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
    }

    @Test
    public void creating_a_group_works() {
        group = new Group.Builder(IRRELEVANT).build();
        Group groupInDb = OSIAM_CONNECTOR
                .getGroup(OSIAM_CONNECTOR.createGroup(group, accessToken).getId(), accessToken);
        assertThat(groupInDb.getDisplayName(), is(equalTo(group.getDisplayName())));
    }

    @Test
    public void create_group_with_existing_id_is_ignored() {
        String existingGroupId = "69e1a5dc-89be-4343-976c-b5541af249f4";
        group = new Group.Builder(IRRELEVANT).setId(existingGroupId).build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);
        Group groupInDb = OSIAM_CONNECTOR.getGroup(existingGroupId, accessToken);
        assertThat(groupInDb.getDisplayName(), not(equalTo(group.getDisplayName())));
    }

    @Test(expected = NoResultException.class)
    public void create_group_with_provided_id_ignores_provided_id() {
        group = new Group.Builder(IRRELEVANT).setId(INVALID_ID).build();
        Group createdGroup = OSIAM_CONNECTOR.createGroup(group, accessToken);
        assertThat(createdGroup.getId(), not(equalTo(INVALID_ID))); // This might fail once every 8 billion years
        OSIAM_CONNECTOR.getGroup(newId, accessToken);
    }

    @Test
    public void created_group_can_be_found_via_query() {
        Query query = new QueryBuilder().filter("displayName eq \"" + IRRELEVANT + "\"").build();
        group = new Group.Builder(IRRELEVANT).build();
        OSIAM_CONNECTOR.createGroup(group, accessToken);

        Group foundGroup = findSingleGroupByQuery(query);

        assertThat(foundGroup.getDisplayName(), is(equalTo(IRRELEVANT)));
    }

    @Test(expected = NoResultException.class)
    public void delete_group_works() throws Exception {
        OSIAM_CONNECTOR.deleteGroup(VALID_GROUP_ID, accessToken);

        OSIAM_CONNECTOR.getGroup(newId, accessToken);

    }

    @Test(expected = NoResultException.class)
    public void delete_non_existing_group_raises_exception() throws Exception {
        OSIAM_CONNECTOR.deleteGroup(INVALID_ID, accessToken);
    }

    private Group findSingleGroupByQuery(Query query) {
        SCIMSearchResult<Group> result = OSIAM_CONNECTOR.searchGroups(query, accessToken);
        if (result.getResources().size() == 1) {
            return result.getResources().get(0);
        }
        return null;
    }

}
