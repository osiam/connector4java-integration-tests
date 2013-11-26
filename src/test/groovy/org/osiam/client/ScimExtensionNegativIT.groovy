package org.osiam.client

import java.nio.ByteBuffer
import java.util.Formatter.DateTime

import org.osiam.client.exception.ConflictException;
import org.osiam.client.oauth.AccessToken
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT

import spock.lang.Unroll


class ScimExtensionNegativIT extends AbstractIT {

	private static final String URN = "extension"
	
	private static final String FIELD_NAME_STRING = 'gender'
	private static final String FIELD_TYPE_STRING = 'String'
	private static final String FIELD_NAME_BOOLEAN = 'newsletter'
	private static final String FIELD_TYPE_BOOLEAN = 'Boolean'
	private static final String FIELD_NAME_INTEGER = 'age'
	private static final String FIELD_TYPE_INTEGER = 'Integer'
	private static final String FIELD_NAME_DECIMAL = 'weight'
	private static final String FIELD_TYPE_DECIMAL = 'Decimal'
	private static final String FIELD_NAME_DATE = 'birthday'
	private static final String FIELD_TYPE_DATE = 'Date'
	private static final String FIELD_NAME_BINARY = 'photo'
	private static final String FIELD_TYPE_BINARY = 'binary'
	private static final String FIELD_NAME_REFERENCE = 'mother'
	private static final String FIELD_TYPE_REFERENCE = 'reference'

	private static final String     STRING_VALUE = '!@#$%^&*_+'
	private static final Boolean    BOOLEAN_VALUE = true
	private static final BigInteger INTEGER_VALUE = 123G
	private static final BigDecimal DECIMAL_VALUE = 1.23G
	private static final Date       DATE_TIME_VALUE = createDate(2011, 7, 1, 18, 29, 49)
	private static final ByteBuffer BINARY_VALUE = ByteBuffer.wrap([101, 120, 97, 109, 112, 108, 101] as byte[])
	private static final URI        REFERENCE_VALUE = new URI('https://example.com/Users/28')
	
	
	def setup() {
		testSetup('/database_seeds/ScimExtensionIT/extensions.xml')
	}
	
	@Unroll
	def 'Saving the value \'#fieldValue\' into the extension field #fieldName which is from type #fieldType raises a exception'() {
		given: "a valid access token"
		accessToken = osiamConnector.retrieveAccessToken()
		Extension extension = new Extension(URN);
		extension.addOrUpdateField(fieldName, fieldValue);
		def fdsjkfhs
		
		User.Builder userBuilder = new User.Builder("irrelevant").addExtension(extension);
		
		when:
		osiamConnector.createUser(userBuilder.build(), accessToken)
		
		then:
		thrown(ConflictException)
		
		where:
		fieldName            |fieldType             | fieldValue
		FIELD_NAME_STRING    | FIELD_TYPE_STRING    | BOOLEAN_VALUE
		FIELD_NAME_STRING    | FIELD_TYPE_STRING    | INTEGER_VALUE
		FIELD_NAME_STRING    | FIELD_TYPE_STRING    | DECIMAL_VALUE
		
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | STRING_VALUE
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | 'true'
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | INTEGER_VALUE
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | DECIMAL_VALUE
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | DATE_TIME_VALUE
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | BINARY_VALUE
		FIELD_NAME_BOOLEAN   | FIELD_TYPE_BOOLEAN   | REFERENCE_VALUE
		
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | STRING_VALUE
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | '123'
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | BOOLEAN_VALUE
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | DECIMAL_VALUE
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | DATE_TIME_VALUE
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | BINARY_VALUE
		FIELD_NAME_INTEGER   | FIELD_TYPE_INTEGER   | REFERENCE_VALUE
		
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | STRING_VALUE
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | '1.23'
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | BOOLEAN_VALUE
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | INTEGER_VALUE
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | DATE_TIME_VALUE
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | BINARY_VALUE
		FIELD_NAME_DECIMAL   | FIELD_TYPE_DECIMAL   | REFERENCE_VALUE
		
		FIELD_NAME_DATE      | FIELD_TYPE_DATE | STRING_VALUE
		FIELD_NAME_DATE      | FIELD_TYPE_DATE | BOOLEAN_VALUE
		FIELD_NAME_DATE      | FIELD_TYPE_DATE | INTEGER_VALUE
		FIELD_NAME_DATE      | FIELD_TYPE_DATE | DECIMAL_VALUE
		FIELD_NAME_DATE      | FIELD_TYPE_DATE | BINARY_VALUE
				
		FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | STRING_VALUE
		FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | BOOLEAN_VALUE
		FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | INTEGER_VALUE
		FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | DECIMAL_VALUE
		FIELD_NAME_BINARY    | FIELD_TYPE_BINARY    | DATE_TIME_VALUE

		FIELD_NAME_REFERENCE | FIELD_TYPE_REFERENCE | STRING_VALUE
		FIELD_NAME_REFERENCE | FIELD_TYPE_REFERENCE | BOOLEAN_VALUE
		FIELD_NAME_REFERENCE | FIELD_TYPE_REFERENCE | INTEGER_VALUE
		FIELD_NAME_REFERENCE | FIELD_TYPE_REFERENCE | DECIMAL_VALUE
	}
	
	private static Date createDate(int year, int month, int date, int hourOfDay, int minute,
		int second) {
		Calendar calendar = Calendar.getInstance()
		calendar.set(Calendar.MILLISECOND, 0)
		calendar.setTimeZone(TimeZone.getTimeZone(TimeZone.GMT_ID))
		calendar.set(year, month, date, hourOfDay, minute, second)
		calendar.getTime()
	}
	   
}