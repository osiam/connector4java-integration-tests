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

package org.osiam.client.integration;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.AbstractIntegrationTestBase;
import org.osiam.client.query.Query;
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
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class SearchByExtensionIT extends AbstractIntegrationTestBase {

    @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/extensions.xml")
    public void searching_by_multiple_fields_works() {
        Query query = new Query.Builder(User.class).setFilter("userName co \"existing\" AND extension.gender eq \"male\" AND extension.birthday pr").build();

        SCIMSearchResult<User> result = oConnector.searchUsers(query, accessToken);

        assertThat(result.getTotalResults(), is(1L));
    }
    
    @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/search_by_extensions_with_not.xml")
    public void search_user_with_not_returns_right_user() {
        String query = encodeExpected("not (extension.gender eq \"male\")");
        SCIMSearchResult<User> queryResult = oConnector.searchUsers("filter=" + query, accessToken);
        assertThat(queryResult.getTotalResults(), is(equalTo(1L)));
        assertThat(queryResult.getResources().get(0).getUserName(), is(equalTo("existing3")));
    }
    
    @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/search_by_extensions_with_not.xml")
    public void search_user_with_not_and_present_returns_right_user() {
        String query = encodeExpected("not (extension.gender pr)");
        SCIMSearchResult<User> queryResult = oConnector.searchUsers("filter=" + query + "&sortBy=username", accessToken);
        assertThat(queryResult.getTotalResults(), is(equalTo(2L)));
        assertThat(queryResult.getResources().get(0).getUserName(), is(equalTo("existing2")));
        assertThat(queryResult.getResources().get(1).getUserName(), is(equalTo("marissa")));
    }
    
    @Test
    @DatabaseSetup(value = "/database_seeds/SearchByExtensionIT/search_by_extensions_with_not.xml")
    public void search_user_with_not_and_present_and_equal_field_returns_right_user() {
        String query = encodeExpected("not (extension.gender pr and extension.gender eq \"male\")");
        SCIMSearchResult<User> queryResult = oConnector.searchUsers("filter=" + query + "&sortBy=username", accessToken);
        assertThat(queryResult.getTotalResults(), is(equalTo(3L)));
        assertThat(queryResult.getResources().get(0).getUserName(), is(equalTo("existing2")));
        assertThat(queryResult.getResources().get(1).getUserName(), is(equalTo("existing3")));
        assertThat(queryResult.getResources().get(2).getUserName(), is(equalTo("marissa")));
    }
}
