package org.osiam.test.integration

import org.osiam.client.exception.ConflictException
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.User
import spock.lang.Unroll

class ScimExtensionTypesIT extends AbstractExtensionBaseIT {

    def setup() {
        setupDatabase('/database_seeds/ScimExtensionIT/extensions.xml')
    }

    @Unroll
    def 'Saving \'#fieldValue\' into the extension field \'#fieldName\' which is from type #fieldType raises a exception'() {
        given: "a valid access token"
        Extension extension = new Extension(URN);
        extension.addOrUpdateField(fieldName, fieldValue);

        User.Builder userBuilder = new User.Builder("irrelevant").addExtension(extension);

        when:
        osiamConnector.createUser(userBuilder.build(), accessToken)

        then:
        thrown(ConflictException)

        where:
        fieldName            | fieldType            | fieldValue
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | STRING_VALUE
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | INTEGER_VALUE
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | DECIMAL_VALUE
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | DATE_TIME_VALUE
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | BINARY_VALUE
        FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | REFERENCE_VALUE

        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | STRING_VALUE
        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | BOOLEAN_VALUE
        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | DECIMAL_VALUE
        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | DATE_TIME_VALUE
        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | BINARY_VALUE
        FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | REFERENCE_VALUE

        FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | STRING_VALUE
        FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | BOOLEAN_VALUE
        FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | DATE_TIME_VALUE
        FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | BINARY_VALUE
        FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | REFERENCE_VALUE

        FIELD_NAME_DATE      | FIELD_TYPE_DATE      | STRING_VALUE
        FIELD_NAME_DATE      | FIELD_TYPE_DATE      | BOOLEAN_VALUE
        FIELD_NAME_DATE      | FIELD_TYPE_DATE      | INTEGER_VALUE
        FIELD_NAME_DATE      | FIELD_TYPE_DATE      | DECIMAL_VALUE
        FIELD_NAME_DATE      | FIELD_TYPE_DATE      | BINARY_VALUE

        FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | STRING_VALUE
        FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | DECIMAL_VALUE
        FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | DATE_TIME_VALUE

        FIELD_NAME_REFERENCE | FIELD_TYPE_REFERENCE | STRING_VALUE
    }

}