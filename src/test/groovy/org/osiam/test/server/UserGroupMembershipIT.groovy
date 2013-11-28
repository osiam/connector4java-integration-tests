package org.osiam.test.server

import org.osiam.client.update.UpdateGroup
import org.osiam.client.update.UpdateUser
import org.osiam.resources.scim.Group
import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import org.osiam.test.AbstractIT

/**
 * Test to ensure that updating group membership works.
 * User: Jochen Todea
 * Date: 28.11.13
 * Time: 11:24
 * Created: with Intellij IDEA
 */
class UserGroupMembershipIT extends AbstractIT {

    def setupSpec() {
        setupDatabase("database_seed_user_group_membership.xml")
    }

    def "add member to group"(){
        given:
        def user = new User.Builder("testUser").setPassword("test").build()
        def group = new Group.Builder().setDisplayName("testGroup").build()
        def createdUser = osiamConnector.createUser(user, osiamConnector.retrieveAccessToken())
        def createdGroup = osiamConnector.createGroup(group, osiamConnector.retrieveAccessToken())

        def updateGroup = new UpdateGroup.Builder()
                .addMember(createdUser.getId())
                .build()

        when:
        def updatedGroup = osiamConnector.updateGroup(createdGroup.getId(), updateGroup, osiamConnector.retrieveAccessToken())

        then:
        updatedGroup.getMembers().size() == 1

        def theUserWithGroup = osiamConnector.getUser(createdUser.getId(), osiamConnector.retrieveAccessToken())
        theUserWithGroup.getGroups().size() == 1

        def theGroupWithMembers = osiamConnector.getGroup(createdGroup.getId(), osiamConnector.retrieveAccessToken())
        theGroupWithMembers.getMembers().size() == 1
    }

    //for further information see:
    //http://tools.ietf.org/html/draft-ietf-scim-core-schema-00#section-6.2
    //users group field: "Since this attribute is read-only, group membership changes MUST be applied via the Group Resource."
    def "add group membership to user is not allowed by the scim specification"(){
        given:
        def user = new User.Builder("testUser1").setPassword("test").build()
        def group = new Group.Builder().setDisplayName("testGroup1").build()
        def createdUser = osiamConnector.createUser(user, osiamConnector.retrieveAccessToken())
        def createdGroup = osiamConnector.createGroup(group, osiamConnector.retrieveAccessToken())

        MultiValuedAttribute groupMembership = new MultiValuedAttribute.Builder().setValue(createdGroup.getId()).build();
        def updateUser = new UpdateUser.Builder()
                .addGroupMembership(groupMembership)
                .build()

        when:
        def updatedUser = osiamConnector.updateUser(createdUser.getId(), updateUser, osiamConnector.retrieveAccessToken())

        then:
        updatedUser.getGroups().size() == 0

        def theUserWithGroup = osiamConnector.getUser(createdUser.getId(), osiamConnector.retrieveAccessToken())
        theUserWithGroup.getGroups().size() == 0

        def theGroupWithMembers = osiamConnector.getGroup(createdGroup.getId(), osiamConnector.retrieveAccessToken())
        theGroupWithMembers.getMembers().size() == 0
    }
}