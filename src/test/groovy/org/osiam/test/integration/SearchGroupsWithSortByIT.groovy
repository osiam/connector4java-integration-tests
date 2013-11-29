package org.osiam.test.integration

import org.osiam.client.query.Query
import org.osiam.client.query.metamodel.Group_
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
        Query.Builder queryBuilder = new Query.Builder(Group)
        queryBuilder.setSortBy(sortBy)

        when:
        SCIMSearchResult<Group> queryResult = osiamConnector.searchGroups(queryBuilder.build(), accessToken)

        then:
        queryResult.resources.size() == expectedOrder.size()
        resultsAreInRightOrder(queryResult.resources, expectedOrder)

        where:
        sortBy                   | expectedOrder
        Group_.displayName       | ['test_group01', 'test_group02', 'test_group03'] as List
        Group_.externalId        | ['test_group03', 'test_group02', 'test_group01'] as List

        Group_.Meta.created      | ['test_group01', 'test_group02', 'test_group03'] as List
        Group_.Meta.lastModified | ['test_group03', 'test_group02', 'test_group01'] as List
        Group_.Meta.location     | ['test_group03', 'test_group01', 'test_group02'] as List
    }

    private def void resultsAreInRightOrder(List<Group> groups, List<String> expectedGroupNameOrder) {
        [groups, expectedGroupNameOrder].transpose().each { it ->
            assert it[0].displayName == it[1]
        }
    }

}