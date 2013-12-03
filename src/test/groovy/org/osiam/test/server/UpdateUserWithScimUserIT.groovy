package org.osiam.test.server

import org.osiam.resources.scim.Extension
import org.osiam.resources.scim.ExtensionFieldType
import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import org.osiam.test.integration.AbstractIT

/**
 * CHANGE THIS TEXT TO SOMETHING USEFUL, DESCRIBING THE CLASS.
 * User: Jochen Todea
 * Date: 03.12.13
 * Time: 11:31
 * Created: with Intellij IDEA
 */
class UpdateUserWithScimUserIT extends AbstractIT {

    def urn = "urn:scim:schemas:osiam:1.0:Registration"
    def field = "tempMail"

    def setupSpec() {
        setupDatabase("database_seed_change_email.xml")
    }

    def "updating a user should also work with user object instead of updateUser object"() {
        given:
        def user = new User.Builder("testUser").setPassword("test").build()
        def createdUser = osiamConnector.createUser(user, osiamConnector.retrieveAccessToken())

        def extension = new Extension(urn)
        extension.addOrUpdateField(field, "value")

        def updateUser = new User.Builder("testUser").setDisplayName("display")
                .setPhoneNumbers([new MultiValuedAttribute.Builder().setValue("test").setType("work").build()] as List)
                .setActive(true).addExtension(urn, extension).build()

        when:
        def result = osiamConnector.updateUser(createdUser.getId(), updateUser, osiamConnector.retrieveAccessToken())

        then:
        result.isActive()
        def savedUser = osiamConnector.getUser(createdUser.getId(), osiamConnector.retrieveAccessToken())
        savedUser.isActive()
        savedUser.getPhoneNumbers().size() == 1
        savedUser.getDisplayName() == "display"
        savedUser.getUserName() == "testUser"
        savedUser.getExtension(urn).getField(field, ExtensionFieldType.STRING).equals("value")
    }

}