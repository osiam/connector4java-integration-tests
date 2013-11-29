package org.osiam.test.integration

import org.osiam.resources.scim.SCIMSearchResult
import spock.lang.Unroll


class ScimExtensionPresentSearchIT extends AbstractExtensionBaseIT {

    def setup() {
        setupDatabase('/database_seeds/ScimExtensionSearchIT/user_by_present_extension.xml')
    }

    @Unroll
    def 'search for user by #fieldType extension and constraint \'pr\' with query string works'() {

        given:
        def query = URLEncoder.encode("extension.$fieldName pr", 'UTF-8');

        when:
        SCIMSearchResult result = osiamConnector.searchUsers("filter=$query", accessToken)

        then:
        result.totalResults == 1

        where:
        fieldType            | fieldName
        FIELD_TYPE_STRING    | FIELD_NAME_STRING
        FIELD_TYPE_INTEGER   | FIELD_NAME_INTEGER
        FIELD_TYPE_DECIMAL   | FIELD_NAME_DECIMAL
        FIELD_TYPE_BOOLEAN   | FIELD_NAME_BOOLEAN
        FIELD_TYPE_DATE      | FIELD_NAME_DATE
        FIELD_TYPE_BINARY    | FIELD_NAME_BINARY
        FIELD_TYPE_REFERENCE | FIELD_NAME_REFERENCE
    }
}
