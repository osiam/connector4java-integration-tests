package de.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamGroupService;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.Group_;
import org.osiam.resources.scim.Group;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class SearchGroupServiceIT extends AbstractIntegrationTestBase {

    private static final String EXPECTED_GROUP_NAME = "test_group01";
	static private String VALID_GROUP_UUID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private QueryResult<Group> queryResult;

    @Test
    public void search_for_group_by_string() {
        String searchString = encodeExpected("displayName eq "+ EXPECTED_GROUP_NAME);
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsOnlyValidGroup();
    }

    @Test
    public void search_for_group_by_non_used_displayName() {
        String searchString = encodeExpected("displayName eq " + INVALID_STRING);
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsNoValidUser();
    }

    @Test
    public void search_for_group_with_querybuilder() throws UnsupportedEncodingException {
        Query.Filter filter = new Query.Filter(Group.class);
        filter.startsWith(Group_.displayName.equalTo(EXPECTED_GROUP_NAME));
        Query query = new Query.Builder(Group.class)
                .filter(filter).build();

        whenSingleGroupIsSearchedByQueryBuilder(query);
        queryResultContainsOnlyValidGroup();
    }

    private void queryResultContainsOnlyValidGroup() {
        assertEquals(queryResult.getTotalResults(), 1);
        queryResultContainsValidGroup();
    }

    private void queryResultContainsValidGroup() {
        for (Group actGroup : queryResult.getResources()) {
            if (actGroup.getId().equals(VALID_GROUP_UUID)) {
                return; // OK
            }
        }
        fail("Valid group could not be found.");
    }

    private void queryResultContainsNoValidUser() {
        assertEquals(queryResult.getTotalResults(), 0);
    }

    private void whenSingleGroupIsSearchedByQueryString(String queryString) {
        queryResult = service.searchGroups("filter=" + queryString, accessToken);
    }

    private void whenSingleGroupIsSearchedByQueryBuilder(Query query) {
        queryResult = service.searchGroups(query, accessToken);
    }
}
