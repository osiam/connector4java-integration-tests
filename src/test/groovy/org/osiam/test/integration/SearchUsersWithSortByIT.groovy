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

import org.osiam.client.query.Query
import org.osiam.client.query.metamodel.User_
import org.osiam.resources.scim.SCIMSearchResult
import org.osiam.resources.scim.User
import spock.lang.Unroll

class SearchUsersWithSortByIT extends AbstractIT {

    def setup() {
        setupDatabase('/database_seeds/SearchUsersWithSortByIT/database_seed.xml')
    }

    @Unroll
    def 'searching for all Users with sortBy field set to #sortBy and default sort order should work'() {
        given:
        Query.Builder queryBuilder = new Query.Builder(User)
        queryBuilder.setSortBy(sortBy)

        when:
        SCIMSearchResult<User> queryResult = osiamConnector.searchUsers(queryBuilder.build(), accessToken)

        then:
        queryResult.resources.size() == expectedOrder.size()
        resultsAreInRightOrder(queryResult.resources, expectedOrder)

        where:
        sortBy                  | expectedOrder
        User_.userName          | ['bjensen', 'jcambell', 'marissa'] as List
        User_.displayName       | ['marissa', 'bjensen', 'jcambell'] as List
        User_.externalId        | ['bjensen', 'jcambell', 'marissa'] as List
        User_.locale            | ['bjensen', 'jcambell', 'marissa'] as List
        User_.nickName          | ['bjensen', 'jcambell', 'marissa'] as List
        User_.preferredLanguage | ['bjensen', 'jcambell', 'marissa'] as List
        User_.profileUrl        | ['bjensen', 'jcambell', 'marissa'] as List
        User_.timezone          | ['bjensen', 'jcambell', 'marissa'] as List
        User_.title             | ['bjensen', 'jcambell', 'marissa'] as List
        User_.userType          | ['jcambell', 'marissa', 'bjensen'] as List

        User_.Meta.created      | ['marissa', 'jcambell', 'bjensen'] as List
        User_.Meta.lastModified | ['bjensen', 'jcambell', 'marissa'] as List
        User_.Meta.location     | ['jcambell', 'bjensen', 'marissa'] as List

        User_.Name.familyName   | ['marissa', 'bjensen', 'jcambell'] as List
        User_.Name.formatted    | ['marissa', 'bjensen', 'jcambell'] as List
        User_.Name.givenName    | ['marissa', 'bjensen', 'jcambell'] as List
    }

    private def void resultsAreInRightOrder(List<User> users, List<String> expectedUserNameOrder) {
        [users, expectedUserNameOrder].transpose().each { it ->
            assert it[0].userName == it[1]
        }
    }

}