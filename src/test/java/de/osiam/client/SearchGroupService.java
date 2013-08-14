package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamGroupService;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryBuilder;
import org.osiam.client.query.QueryResult;
import org.osiam.resources.scim.Group;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class SearchGroupService extends AbstractIntegrationTestBase {

    static private String VALID_GROUP_UUID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    private QueryResult<Group> queryResult;
    private OsiamGroupService service;

    @Before
    public void setUp() throws URISyntaxException {
        service = new OsiamGroupService.Builder(endpointAddress).build();
    }

    @Test
    public void search_for_group_by_displayName() {
        String searchString = "displayName eq test_group01";
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsOnlyValidGroiup();
    }

    @Test
    public void search_for_group_by_non_used_displayName() {
        String searchString = "displayName eq thisIsNoGroup";
        whenSingleGroupIsSearchedByQueryString(searchString);
        queryResultContainsNoValidUser();
    }

    @Test
    public void search_for_group_by_querybuilder_and_displayName() {
        Query query = new QueryBuilder(Group.class)
                .filter("displayName").equalTo("test_group01").build();

        whenSingleGroupIsSearchedByQueryBuilder(query);
        queryResultContainsOnlyValidGroiup();
    }

    private void queryResultContainsValidGroup() {
        assertTrue(queryResult != null);
        for (Group actGroup : queryResult.getResources()) {
            if (actGroup.getId().equals(VALID_GROUP_UUID)) {
                return; // OK
            }
        }
        fail("Valid group could not be found.");
    }

    private void queryResultContainsOnlyValidGroiup() {
        assertTrue(queryResult != null);
        assertEquals(queryResult.getTotalResults(), 1);
        queryResultContainsValidGroup();
    }

    private void queryResultContainsNoValidUser() {
        assertTrue(queryResult != null);
        assertEquals(queryResult.getTotalResults(), 0);
    }

    private void whenSingleGroupIsSearchedByQueryString(String queryString) {
        queryResult = service.searchGroups("filter=" + queryString, accessToken);
    }

    private void whenSingleGroupIsSearchedByQueryBuilder(Query query) {
        queryResult = service.searchGroups(query, accessToken);
    }
}
