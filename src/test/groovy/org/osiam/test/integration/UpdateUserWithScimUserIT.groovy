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