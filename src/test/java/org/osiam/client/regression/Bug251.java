package org.osiam.client.regression;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.AbstractIntegrationTestBase;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/database_seeds/Bug251/database_seed.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class Bug251 extends AbstractIntegrationTestBase {

    @Test
    public void sorting_by_formatted_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.formatted")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }

    @Test
    public void sorting_by_familyName_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.familyName")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }

    @Test
    public void sorting_by_givenName_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.givenName")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }

    @Test
    public void sorting_by_middleName_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.middleName")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }

    @Test
    public void sorting_by_honorificPrefix_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.honorificPrefix")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }

    @Test
    public void sorting_by_honorificSuffix_does_not_remove_users_without_a_name_set_from_result() {
        Query query = new QueryBuilder()
                .ascending("name.honorificSuffix")
                .build();

        SCIMSearchResult<User> result = OSIAM_CONNECTOR.searchUsers(query, accessToken);

        assertThat(result.getResources().size(), is(equalTo(2)));
    }
}
