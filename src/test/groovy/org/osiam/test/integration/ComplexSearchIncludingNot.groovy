package org.osiam.test.integration

import org.osiam.client.query.Query
import org.osiam.client.query.metamodel.Comparison
import org.osiam.resources.scim.SCIMSearchResult
import org.osiam.resources.scim.User

/**
 * Some complex search criteria including the not operator.
 * User: Jochen Todea
 * Date: 12.12.13
 * Time: 16:36
 * Created: with Intellij IDEA
 */
class ComplexSearchIncludingNot extends AbstractIT {

    def setup() {
        setupDatabase("database_seed_parser_with_not.xml");
    }

    def "search with not operator"() {
        given:
        Query.Filter notFilter2 = new Query.Filter(User).not(new Comparison("groups.display eq \"stipendiat\""))
        Query.Filter notFilter3 = new Query.Filter(User).not(new Comparison("groups.display eq \"anwaerter\""))

        Query.Filter notFilter = new Query.Filter(User).not(new Comparison("groups.display eq \"bewerber\"")).and(notFilter2).and(notFilter3)
        Query.Filter filter = new Query.Filter(User, notFilter).and(new Comparison("active eq \"true\""))
        Query query = new Query.Builder(User).setFilter(filter).build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken);

        then:
        result.getResources().size() == 1
        result.getResources().each {
            assert it.getUserName().equals("george0")
        }
    }

    def "combined search with 'and' and 'or'"() {
        given:
        Query.Filter innerFilter = new Query.Filter(User, new Comparison("emails.value eq \"jj@tt.de\""))
                .or(new Comparison("emails.value eq \"oo@aa.de\"")).or(new Comparison("emails.value eq \"bb@ss.de\""))
        Query.Filter filter = new Query.Filter(User, new Comparison("groups.display eq \"stipendiat\"")).and(innerFilter)

        Query query = new Query.Builder(User).setFilter(filter).build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken);

        then:
        result.getResources().size() == 2
        result.getResources().each {
            assert (it.getUserName().equals("george4") || it.getUserName().equals("george5"))
        }
    }

    def "searching with escaped quot in value"() {
        given:
        Query query = new Query.Builder(User).setFilter('userName eq "hanz \\"meiser\\""').build()

        when:
        SCIMSearchResult<User> result = osiamConnector.searchUsers(query, accessToken);

        then:
        result.getResources().size() == 1
        result.getResources().each {
            assert it.getUserName().equals("hanz \"meiser\"")
        }
    }
}