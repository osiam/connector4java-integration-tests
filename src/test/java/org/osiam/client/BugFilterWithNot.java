package org.osiam.client;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.QueryBuilder;
import org.osiam.resources.scim.Group;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup("/database_seeds/SearchUserServiceIT/database_seed_bug_filter_with_not.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class BugFilterWithNot extends AbstractIntegrationTestBase {
    private static final String ADMIN_USER_NAME = "marissa";
    private static final String ADMIN_USER_PASSWORD = "koala";
    private static final String GROUP_TWO_NAME = "test_group02";
    private static final String TEST_USER_NAME = "marissa";

    private AccessToken accessToken;

    @Before
    public void setup() {
        accessToken = OSIAM_CONNECTOR.retrieveAccessToken(ADMIN_USER_NAME, ADMIN_USER_PASSWORD, Scope.ADMIN);
    }

    @Ignore("This test fails at the moment because the bug is not yet fixed. @Ignore must be removed after the bug-fix.")
    @Test
    public void filter_groups_where_the_user_is_not_included_in() throws Exception {
        final SCIMSearchResult<Group> result = searchGroup("not(members eq \"cef9452e-00a9-4cec-a086-d171374ffbef\")");

        final Group expectedGroup = new Group.Builder(GROUP_TWO_NAME).build();

        assertThat("Can not find expected group test_group02!",
                result.getResources(),
                hasItem(expectedGroup));
    }

    @Ignore("This test fails at the moment because the bug is not yet fixed. @Ignore must be removed after the bug-fix.")
    @Test
    public void filter_users_where_not_a_member_of_group() throws Exception {
        final SCIMSearchResult<User> result = searchUser("not(groups eq \"44e8645f-a0ff-4b5b-849e-d9f83b9a9bd9\")");

        final User expectedUser = new User.Builder(TEST_USER_NAME).setId("cef9452e-00a9-4cec-a086-d171374ffbef")
                .build();

        assertThat("The user marissa is in the group test_group02 but should not be!",
                result.getResources(),
                not(hasItem(expectedUser)));
    }

    private SCIMSearchResult<Group> searchGroup(String filter) {
        QueryBuilder qb = new QueryBuilder();
        qb.filter(filter);

        return OSIAM_CONNECTOR.searchGroups(qb.build(), accessToken);
    }

    private SCIMSearchResult<User> searchUser(String filter) {
        QueryBuilder qb = new QueryBuilder();
        qb.filter(filter);

        return OSIAM_CONNECTOR.searchUsers(qb.build(), accessToken);
    }
}
