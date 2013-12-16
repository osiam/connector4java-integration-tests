package org.osiam.client.regression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.AbstractIntegrationTestBase;
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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
@DatabaseSetup(value = "/database_seeds/BT22IT/user_by_extension.xml")
public class BT22IT extends AbstractIntegrationTestBase {

    private static final String URN = "ExtensionWithMixedCase";
    private static final String URN_WRONG_CASE = URN.toUpperCase(Locale.ENGLISH);

    private static final String FIELD = "gender";
    private static final String FIELD_WRONG_CASE = FIELD.toUpperCase(Locale.ENGLISH);

    @Test
    public void searching_for_user_with_filter_on_extension_field_same_case() throws UnsupportedEncodingException {
        String query = URLEncoder.encode(URN + "." + FIELD + " eq \"female\"", "UTF-8");

        SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(3L)));
    }

    @Test
    public void searching_for_user_with_filter_on_extension_field_with_wrong_cased_urn()
            throws UnsupportedEncodingException {
        String query = URLEncoder.encode(URN_WRONG_CASE + "." + FIELD + " eq \"female\"", "UTF-8");

        SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(3L)));
    }

    @Test
    public void searching_for_user_with_filter_on_extension_field_with_wrong_cased_field_name()
            throws UnsupportedEncodingException {
        String query = URLEncoder.encode(URN + "." + FIELD_WRONG_CASE + " eq \"female\"", "UTF-8");

        SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(3L)));
    }

    @Test
    public void searching_for_user_with_filter_on_extension_field_with_wrong_cased_urn_and_field_name()
            throws UnsupportedEncodingException {
        String query = URLEncoder.encode(URN_WRONG_CASE + "." + FIELD_WRONG_CASE + " eq \"female\"", "UTF-8");

        SCIMSearchResult<User> result = oConnector.searchUsers("filter=" + query, accessToken);

        assertThat(result.getTotalResults(), is(equalTo(3L)));
    }

}
