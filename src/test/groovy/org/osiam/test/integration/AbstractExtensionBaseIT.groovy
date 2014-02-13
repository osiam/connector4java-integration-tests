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

import org.joda.time.format.ISODateTimeFormat

import java.nio.ByteBuffer

class AbstractExtensionBaseIT extends AbstractIT {

    static final String URN = "extension"

    static final String FIELD_NAME_STRING = 'gender'
    static final String FIELD_TYPE_STRING = 'String'
    static final String FIELD_NAME_BOOLEAN = 'newsletter'
    static final String FIELD_TYPE_BOOLEAN = 'Boolean'
    static final String FIELD_NAME_INTEGER = 'age'
    static final String FIELD_TYPE_INTEGER = 'Integer'
    static final String FIELD_NAME_DECIMAL = 'weight'
    static final String FIELD_TYPE_DECIMAL = 'Decimal'
    static final String FIELD_NAME_DATE = 'birthday'
    static final String FIELD_TYPE_DATE = 'Date'
    static final String FIELD_NAME_BINARY = 'photo'
    static final String FIELD_TYPE_BINARY = 'binary'
    static final String FIELD_NAME_REFERENCE = 'mother'
    static final String FIELD_TYPE_REFERENCE = 'reference'

    static final String STRING_VALUE = '!@#$%^&*_+'
    static final Boolean BOOLEAN_VALUE = true
    static final BigInteger INTEGER_VALUE = 123G
    static final BigDecimal DECIMAL_VALUE = 1.23G
    static final Date DATE_TIME_VALUE = createDate(2011, 7, 1, 18, 29, 49)
    static final ByteBuffer BINARY_VALUE = ByteBuffer.wrap([
            101,
            120,
            97,
            109,
            112,
            108,
            101] as byte[])
    static final URI REFERENCE_VALUE = new URI('https://example.com/Users/28')

    static Date createDate(int year, int month, int date, int hourOfDay, int minute,
                           int second) {
        Calendar calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.GMT_ID))
        calendar.set(year, month, date, hourOfDay, minute, second)
        calendar.getTime()
    }

    static String dateAsString(int year, int month, int date, int hourOfDay, int minute,
                               int second) {
        Date completeDate = createDate(year, month, date, hourOfDay, minute, second)
        ISODateTimeFormat.dateTime().withZoneUTC().print(completeDate.time)
    }
}
