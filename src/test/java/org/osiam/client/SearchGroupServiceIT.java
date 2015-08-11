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
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.SCIMSearchResult;
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
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class SearchGroupServiceIT extends AbstractIntegrationTestBase {

    private static final String EXPECTED_GROUP_NAME = "test_group01";
    private static String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private SCIMSearchResult<Group> queryResult;

    @Before
    public void setUp() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken("marissa", "koala", Scope.ADMIN);
    }

    @Test
    public void search_for_group_by_string() {
        Query search = new QueryBuilder().filter("displayName eq \"" + EXPECTED_GROUP_NAME + "\""
                + " and externalid eq \"ext_id_test_group01\""
                + " and meta.created eq \"" + dateAsString(2013, 6, 31, 21, 43, 18, 0) + "\""
                + " and meta.lastmodified eq \"" + dateAsString(2013, 6, 31, 21, 43, 18, 0) + "\""
                + " and members eq \"834b410a-943b-4c80-817a-4465aed037bc\"").build();
        whenSingleGroupIsSearchedByQueryString(search);
        assertThatQueryResultContainsOnlyValidGroup();
    }

    @Test
    public void search_for_group_by_non_used_displayName() {
        Query search = new QueryBuilder().filter("displayName eq \"" + INVALID_STRING + "\"").build();
        whenSingleGroupIsSearchedByQueryString(search);
        assertThatQueryResultContainsNoValidUser();
    }

    @Test
    public void search_for_group_with_querybuilder() throws UnsupportedEncodingException {
        Query query = new QueryBuilder().filter("displayName eq \"" + EXPECTED_GROUP_NAME + "\"").build();

        whenSingleGroupIsSearchedByQueryBuilder(query);
        assertThatQueryResultContainsOnlyValidGroup();
    }

    private void assertThatQueryResultContainsOnlyValidGroup() {
        assertEquals(queryResult.getTotalResults(), 1);
        assertThatQueryResultContainsValidGroup();
    }

    private void assertThatQueryResultContainsValidGroup() {
        for (Group actGroup : queryResult.getResources()) {
            if (actGroup.getId().equals(VALID_GROUP_ID)) {
                return; // OK
            }
        }
        fail("Valid group could not be found.");
    }

    private void assertThatQueryResultContainsNoValidUser() {
        assertEquals(queryResult.getTotalResults(), 0);
    }

    private void whenSingleGroupIsSearchedByQueryString(Query query) {
        queryResult = OSIAM_CONNECTOR.searchGroups(query, accessToken);
    }

    private void whenSingleGroupIsSearchedByQueryBuilder(Query query) {
        queryResult = OSIAM_CONNECTOR.searchGroups(query, accessToken);
    }
}
