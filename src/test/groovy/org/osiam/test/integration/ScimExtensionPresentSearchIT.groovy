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
import org.osiam.resources.scim.SCIMSearchResult

import spock.lang.Unroll

class ScimExtensionPresentSearchIT extends AbstractExtensionBaseIT {

    def setup() {
        setupDatabase('/database_seeds/ScimExtensionSearchIT/user_by_present_extension.xml')
    }

    @Unroll
    def 'search for user by #fieldType extension and constraint \'pr\' with query string works'() {

        given:
        Query query = new QueryBuilder().filter("extension.$fieldName pr").build()

        when:
        SCIMSearchResult result = OSIAM_CONNECTOR.searchUsers(query, accessToken)

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
