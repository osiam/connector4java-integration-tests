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
import org.osiam.client.query.QueryBuilder
import org.osiam.resources.scim.Group
import org.osiam.resources.scim.SCIMSearchResult

import spock.lang.Unroll

class SearchGroupsWithSortByIT extends AbstractIT {

    def setup() {
        setupDatabase('/database_seeds/SearchGroupsWithSortByIT/database_seed.xml')
    }

    @Unroll
    def 'searching for a Group with sortBy field set to #sortBy and default sort order works'() {
        given:
        Query query = new QueryBuilder().ascending(sortBy).build()

        when:
        SCIMSearchResult<Group> queryResult = OSIAM_CONNECTOR.searchGroups(query, accessToken)

        then:
        queryResult.resources.size() == expectedOrder.size()
        resultsAreInRightOrder(queryResult.resources, expectedOrder)

        where:
        sortBy              | expectedOrder
        'displayName'       | ['test_group01', 'test_group02', 'test_group03'] as List
        'externalId'        | ['test_group03', 'test_group02', 'test_group01'] as List

        'meta.created'      | ['test_group01', 'test_group02', 'test_group03'] as List
        'meta.lastModified' | ['test_group03', 'test_group02', 'test_group01'] as List
        'meta.location'     | ['test_group03', 'test_group01', 'test_group02'] as List
    }

    private def void resultsAreInRightOrder(List<Group> groups, List<String> expectedGroupNameOrder) {
        [groups, expectedGroupNameOrder].transpose().each { it ->
            assert it[0].displayName == it[1]
        }
    }

}