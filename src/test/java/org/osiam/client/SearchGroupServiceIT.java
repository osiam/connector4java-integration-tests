package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.query.Query;
import org.osiam.client.query.metamodel.Group_;
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
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class SearchGroupServiceIT extends AbstractIntegrationTestBase {

    private static final String EXPECTED_GROUP_NAME = "test_group01";
    private static String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private SCIMSearchResult<Group> queryResult;

    @Test
    public void search_for_group_by_string() {
        String searchString = encodeExpected("displayName eq \""+ EXPECTED_GROUP_NAME + "\""
                + " and externalid eq \"ext_id_test_group01\""
                + " and meta.created eq \"" + dateAsString(2013, 6, 31, 21, 43, 18, 0) + "\""
                + " and meta.lastmodified eq \"" + dateAsString(2013, 6, 31, 21, 43, 18, 0) + "\""
                + " and members eq \"834b410a-943b-4c80-817a-4465aed037bc\"");
        whenSingleGroupIsSearchedByQueryString(searchString);
        assertThatQueryResultContainsOnlyValidGroup();
    }

    @Test
    public void search_for_group_by_non_used_displayName() {
        String searchString = encodeExpected("displayName eq \"" + INVALID_STRING + "\"");
        whenSingleGroupIsSearchedByQueryString(searchString);
        assertThatQueryResultContainsNoValidUser();
    }

    @Test
    public void search_for_group_with_querybuilder() throws UnsupportedEncodingException {
        Query.Filter filter = new Query.Filter(Group.class, Group_.displayName.equalTo(EXPECTED_GROUP_NAME));
        Query query = new Query.Builder(Group.class)
                .setFilter(filter).build();

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

    private void whenSingleGroupIsSearchedByQueryString(String queryString) {
        queryResult = oConnector.searchGroups("filter=" + queryString, accessToken);
    }

    private void whenSingleGroupIsSearchedByQueryBuilder(Query query) {
        queryResult = oConnector.searchGroups(query, accessToken);
    }
}
