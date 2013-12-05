package org.osiam.test.integration

import org.osiam.client.update.UpdateGroup
import org.osiam.client.update.UpdateUser
import org.osiam.resources.scim.Group
import org.osiam.resources.scim.Meta
import org.osiam.resources.scim.MultiValuedAttribute
import org.osiam.resources.scim.User
import org.osiam.test.integration.AbstractIT

/**
 * Test to ensure that updating group membership works.
 * User: Jochen Todea
 * Date: 28.11.13
 * Time: 11:24
 * Created: with Intellij IDEA
 */
class UserGroupMembershipIT extends AbstractIT {

    def setup() {
        setupDatabase("database_seed_user_group_membership.xml")
    }

    def "add user member to group"(){
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

    def "add group member to group"(){
        given:
        def parentGroup = new Group.Builder().setDisplayName("parentGroup").build()
        def memberGroup = new Group.Builder().setDisplayName("memberGroup").build()
        def createdParentGroup = osiamConnector.createGroup(parentGroup, osiamConnector.retrieveAccessToken())
        def createdMemberGroup = osiamConnector.createGroup(memberGroup, osiamConnector.retrieveAccessToken())

        def updateGroup = new UpdateGroup.Builder()
                .addMember(createdMemberGroup.getId())
                .build()

        when:
        def updatedGroup = osiamConnector.updateGroup(createdParentGroup.getId(), updateGroup, osiamConnector.retrieveAccessToken())

        then:
        updatedGroup.getMembers().size() == 1

        def parentGroupWithMembers = osiamConnector.getGroup(createdParentGroup.getId(), osiamConnector.retrieveAccessToken())
        parentGroupWithMembers.getMembers().size() == 1

        def memberGroupWithparent = osiamConnector.getGroup(createdMemberGroup.getId(), osiamConnector.retrieveAccessToken())
        memberGroupWithparent.getMembers().size() == 0
    }

    def "remove member from group"() {
        given:
        def memberGroup1 = osiamConnector.createGroup(new Group.Builder().setDisplayName("memberGroup1").build(), osiamConnector.retrieveAccessToken())
        def memberGroup2 = osiamConnector.createGroup(new Group.Builder().setDisplayName("memberGroup2").build(), osiamConnector.retrieveAccessToken())
        def memberUser = osiamConnector.createUser(new User.Builder("userMember").setPassword("test").build(), osiamConnector.retrieveAccessToken())

        def member1 = new MultiValuedAttribute.Builder().setValue(memberGroup1.getId()).build()
        def member2 = new MultiValuedAttribute.Builder().setValue(memberGroup2.getId()).build()
        def member3 = new MultiValuedAttribute.Builder().setValue(memberUser.getId()).build()
        def parentGroup = new Group.Builder().setDisplayName("parent").setMembers([member1, member2, member3] as Set).build()

        def parent = osiamConnector.createGroup(parentGroup, osiamConnector.retrieveAccessToken())


        def updateGroup = new Group.Builder()
                .setMembers([new MultiValuedAttribute.Builder().setValue(memberGroup1.getId()).setOperation("delete").build()] as Set)
                .build()

        when:
        def result = osiamConnector.updateGroup(parent.getId(), updateGroup, osiamConnector.retrieveAccessToken())

        then:
        parent.getMembers().size() == 3
        result.getMembers().size() == 2
        def persistedParent = osiamConnector.getGroup(parent.getId(), osiamConnector.retrieveAccessToken())
        persistedParent.getMembers().size() == 2
    }

    def "remove all members from group"() {
        given:
        def memberGroup1 = osiamConnector.createGroup(new Group.Builder().setDisplayName("memberGroup10").build(), osiamConnector.retrieveAccessToken())
        def memberGroup2 = osiamConnector.createGroup(new Group.Builder().setDisplayName("memberGroup20").build(), osiamConnector.retrieveAccessToken())

        def member1 = new MultiValuedAttribute.Builder().setValue(memberGroup1.getId()).build()
        def member2 = new MultiValuedAttribute.Builder().setValue(memberGroup2.getId()).build()
        def parentGroup = new Group.Builder().setDisplayName("parent1").setMembers([member1, member2] as Set).build()

        def parent = osiamConnector.createGroup(parentGroup, osiamConnector.retrieveAccessToken())


        def updateGroup = new Group.Builder()
                .setMeta(new Meta.Builder(null, null).setAttributes(["members"] as Set).build())
                .build()

        when:
        def result = osiamConnector.updateGroup(parent.getId(), updateGroup, osiamConnector.retrieveAccessToken())

        then:
        parent.getMembers().size() == 2
        result.getMembers().isEmpty()
        def persistedParent = osiamConnector.getGroup(parent.getId(), osiamConnector.retrieveAccessToken())
        persistedParent.getMembers().isEmpty()
    }

    def "ignoring required and read only attributes"(){
        given:
        def group = new Group.Builder().setDisplayName("Group").build()

        def createdGroup = osiamConnector.createGroup(group, osiamConnector.retrieveAccessToken())

        def updateGroup = new Group.Builder()
                .setMeta(new Meta.Builder(null, null).setAttributes(["displayName", "id"] as Set).build())
                .build()

        when:
        def result = osiamConnector.updateGroup(createdGroup.getId(), updateGroup, osiamConnector.retrieveAccessToken())

        then:
        result.getDisplayName() == "Group"
        result.getId() != null
        def persistedGroup = osiamConnector.getGroup(createdGroup.getId(), osiamConnector.retrieveAccessToken())
        persistedGroup.getDisplayName() == "Group"
        persistedGroup.getId() != null
    }
}