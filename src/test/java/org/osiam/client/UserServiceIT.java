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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Name;
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
@DatabaseSetup("/database_seed_users.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class UserServiceIT extends AbstractIntegrationTestBase {

	private User deserializedUser;

	@Test
	public void all_emails_are_transmitted() {

		whenAValidUserIsDeserialized();
		List<Email> emails = deserializedUser.getEmails();

		assertThat(emails, hasSize(1));
	}

	@Test
	public void emails_are_deserialized_correctly() throws Exception {

		whenAValidUserIsDeserialized();

		Email email = deserializedUser.getEmails().get(0);

		assertThat(email.getValue(), is(equalTo("bjensen@example.com")));
		assertThat(email.getType(), is(equalTo(Email.Type.WORK)));
	}

	@Test
	public void name_is_deserialized_correctly() throws Exception {

		whenAValidUserIsDeserialized();

		Name name = deserializedUser.getName();

		assertThat(name.getFamilyName(), is(equalTo("Jensen")));
		assertThat(name.getFormatted(), is(equalTo("Ms. Barbara J Jensen III")));
		assertThat(name.getGivenName(), is(equalTo("Barbara")));
		assertThat(name.getHonorificPrefix(), is(nullValue()));
		assertThat(name.getHonorificSuffix(), is(nullValue()));
		assertThat(name.getMiddleName(), is(nullValue()));
	}

	@Test
	public void all_addresses_are_transmitted() {

		whenAValidUserIsDeserialized();
		List<Address> addresses = deserializedUser.getAddresses();

		assertThat(addresses, hasSize(2));
	}

	@Test
	public void address_is_deserialized_correctly() {

		whenAValidUserIsDeserialized();

		Address address = deserializedUser.getAddresses().get(0);
		assertThat(address.getCountry(), is(equalTo("Germany")));
		assertThat(address.getLocality(), is(equalTo("Berlin")));
		assertThat(address.getRegion(), is(equalTo("Berlin")));
		assertThat(address.getPostalCode(), is(equalTo("10777")));
		assertThat(address.getStreetAddress(), is(startsWith("Hauptstr. ")));
	}

	@Test
	public void password_is_not_transmitted() throws Exception {
		whenAValidUserIsDeserialized();

		assertThat(deserializedUser.getPassword(), isEmptyString());
	}

	@Test(expected = UnauthorizedException.class)
	public void provide_an_invalid_access_token_raises_exception()
			throws Exception {
		givenAnInvalidAccessToken();

		whenAValidUserIsDeserialized();
		fail("Exception expected");
	}

	@Test(expected = UnauthorizedException.class)
	public void access_token_is_expired() throws Exception {
		givenAnAccessTokenForOneSecond();
		Thread.sleep(1000);
		whenAValidUserIsDeserialized();
		fail("Exception expected");
	}

	private void whenAValidUserIsDeserialized() {
		deserializedUser = oConnector.getUser(VALID_USER_ID, accessToken);
	}

}