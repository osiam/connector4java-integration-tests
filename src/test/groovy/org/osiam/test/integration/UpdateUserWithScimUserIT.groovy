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

import org.osiam.resources.scim.Email
import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.User

class UpdateUserWithScimUserIT extends AbstractIT {

    def urn = 'urn:scim:schemas:osiam:1.0:Registration'
    def field = 'tempMail'

    def setup() {
        setupDatabase('database_seed_change_email.xml')
    }

    def 'updating a user should also work with user object instead of updateUser object'() {
        given:
        def user = new User.Builder('testUser').setPassword('test').build()
        def createdUser = osiamConnector.createUser(user, osiamConnector.retrieveAccessToken())

        def extension = new Extension(urn)
        extension.addOrUpdateField(field, 'value')

        def updateUser = new User.Builder('testUser').setDisplayName('display')
                .setPhoneNumbers([new Email.Builder().setValue('test').setType(Email.Type.WORK).build()] as List)
                .setActive(true).addExtension(extension).build()

        when:
        def result = osiamConnector.updateUser(createdUser.getId(), updateUser, osiamConnector.retrieveAccessToken())

        then:
        result.isActive()
        def savedUser = osiamConnector.getUser(createdUser.getId(), osiamConnector.retrieveAccessToken())
        savedUser.isActive()
        savedUser.getPhoneNumbers().size() == 1
        savedUser.getDisplayName() == 'display'
        savedUser.getUserName() == 'testUser'
        savedUser.getExtension(urn).getField(field, ExtensionFieldType.STRING).equals('value')
    }

}