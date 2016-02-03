package org.osiam.test.integration.regression

import org.osiam.client.exception.BadRequestException
import org.osiam.client.query.QueryBuilder
import org.osiam.test.integration.AbstractIT
import spock.lang.Unroll

class Bug66IT extends AbstractIT {

    def setup() {
        setupDatabase('/database_seed.xml')
    }

    @Unroll
    def "Invalid query '#filter' generates a 400 BAD REQUEST"() {
        given:
        def query = new QueryBuilder().filter(filter).build()

        when:
        OSIAM_CONNECTOR.searchUsers(query, accessToken)

        then:
        thrown(BadRequestException)

        where:
        filter << ["userName = \"marissa\"",
                   "userName eq \"marissa\" and name.formatted = \"Formatted Name\"",
                   "userName = \"marissa\" and name.formatted = \"Formatted Name\""]

    }
}
