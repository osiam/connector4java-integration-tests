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

package org.osiam.test.integration

import org.osiam.client.exception.ConflictException
import org.osiam.client.query.Query
import org.osiam.client.query.metamodel.Comparison
import org.osiam.resources.scim.SCIMSearchResult
import org.osiam.resources.scim.User

/**
 * Some complex search criteria including the not operator.
 */
class ComplexSearchIncludingNotIT extends AbstractIT {

    def setup() {
        setupDatabase('database_seed_parser_with_not.xml')
    }

    def 'search with not operator'() {
        given:
        Query.Filter notFilter2 = new Query.Filter(User).not(new Comparison('groups.display eq \"alumni\"'))
        Query.Filter notFilter3 = new Query.Filter(User).not(new Comparison('groups.display eq \"noob\"'))

        Query.Filter notFilter = new Query.Filter(User).not(new Comparison('groups.display eq \"student\"')).and(notFilter2).and(notFilter3)
        Query.Filter filter = new Query.Filter(User, notFilter).and(new Comparison('active eq \"true\"'))
        Query query = new Query.Builder(User).setFilter(filter).build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken)

        then:
        result.getResources().size() == 2
        def userNames = result.getResources().collect {
			it.userName
		}
		userNames.contains('george0')
		userNames.contains('george2')
    }

    def 'combined search with "and" and "or"'() {
        given:
        Query.Filter innerFilter = new Query.Filter(User, new Comparison('emails.value eq \"jj@tt.de\"'))
                .or(new Comparison('emails.value eq \"oo@aa.de\"')).or(new Comparison('emails.value eq \"bb@ss.de\"'))
        Query.Filter filter = new Query.Filter(User, new Comparison('groups.display eq \"alumni\"')).and(innerFilter)

        Query query = new Query.Builder(User).setFilter(filter).build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken)

        then:
        result.getResources().size() == 2
        result.getResources().each {
            assert (it.getUserName().equals('george4') || it.getUserName().equals('george5'))
        }
    }

    def 'searching with escaped quot in value'() {
        given:
        Query query = new Query.Builder(User).setFilter('userName eq "george \\"alexander\\""').build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken)

        then:
        result.getResources().size() == 1
        result.getResources().each {
            assert it.getUserName().equals('george \"alexander\"')
        }
    }

    def 'search with missing quotes should end up in a exception'(){
        given:
        Query query = new Query.Builder(User).setFilter('userName eq george0').build()

        when:
        osiamConnector.searchUsers(query, accessToken)

        then:
        thrown(ConflictException)
    }
}