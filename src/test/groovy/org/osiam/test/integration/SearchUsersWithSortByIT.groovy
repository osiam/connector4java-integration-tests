package org.osiam.test.integration

import org.osiam.client.oauth.AccessToken
import org.osiam.client.query.Query
import org.osiam.client.query.metamodel.User_
import org.osiam.resources.scim.SCIMSearchResult
import org.osiam.resources.scim.User
import spock.lang.Shared
import spock.lang.Unroll

class SearchUsersWithSortByIT extends AbstractIT {

    @Shared
    AccessToken accessToken

    def setup() {
        testSetup('/database_seeds/SearchUsersWithSortByIT/database_seed.xml')
        accessToken = osiamConnector.retrieveAccessToken()
    }

    @Unroll
    def 'searching for a User with sortBy field set to #sortBy and default sort order works'() {
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