package org.osiam.test.integration

import org.osiam.resources.scim.SCIMSearchResult
import spock.lang.Unroll


public class ScimExtensionSearchIT extends AbstractExtensionBaseIT {

    def setup() {
        testSetup('/database_seeds/ScimExtensionSearchIT/user_by_extension.xml')
    }

    @Unroll
    def 'search for user by #fieldType extension field and constraint #constraint with query string works'() {

        given:
        accessToken = osiamConnector.retrieveAccessToken()
        def query = URLEncoder.encode("extension.$fieldName $constraint $queryValue", 'UTF-8');

        when:
        SCIMSearchResult result = osiamConnector.searchUsers("filter=$query", accessToken)

        then:
        result.getTotalResults() == expectedResult

        where:
        fieldType          | fieldName          | constraint | queryValue                            | expectedResult
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'eq'       | '"female"'                            | 3
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'co'       | 'mal'                                 | 5
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'sw'       | 'fe'                                  | 3
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'gt'       | 'female'                              | 2
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'ge'       | 'male'                                | 2
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'lt'       | 'male'                                | 3
        FIELD_TYPE_STRING  | FIELD_NAME_STRING  | 'le'       | 'female'                              | 3
        FIELD_TYPE_INTEGER | FIELD_NAME_INTEGER | 'eq'       | 30                                    | 1
        FIELD_TYPE_INTEGER | FIELD_NAME_INTEGER | 'gt'       | 30                                    | 1
        FIELD_TYPE_INTEGER | FIELD_NAME_INTEGER | 'ge'       | 30                                    | 2
        FIELD_TYPE_INTEGER | FIELD_NAME_INTEGER | 'lt'       | 30                                    | 3
        FIELD_TYPE_INTEGER | FIELD_NAME_INTEGER | 'le'       | 30                                    | 4
        FIELD_TYPE_DECIMAL | FIELD_NAME_DECIMAL | 'eq'       | 80.7                                  | 1
        FIELD_TYPE_DECIMAL | FIELD_NAME_DECIMAL | 'gt'       | 80.7                                  | 1
        FIELD_TYPE_DECIMAL | FIELD_NAME_DECIMAL | 'ge'       | 80.7                                  | 2
        FIELD_TYPE_DECIMAL | FIELD_NAME_DECIMAL | 'lt'       | 80.7                                  | 3
        FIELD_TYPE_DECIMAL | FIELD_NAME_DECIMAL | 'le'       | 80.7                                  | 4
        FIELD_TYPE_BOOLEAN | FIELD_NAME_BOOLEAN | 'eq'       | true                                  | 3
        FIELD_TYPE_DATE    | FIELD_NAME_DATE    | 'eq'       | dateAsString(1986, 11, 11, 4, 56, 22) | 1
        //FIELD_NAME_DATE    | FIELD_NAME_DATE    | 'lt'       |            |


    }
    //  static final Date DATE_TIME_VALUE = createDate(2011, 7, 1, 18, 29, 49)
}
