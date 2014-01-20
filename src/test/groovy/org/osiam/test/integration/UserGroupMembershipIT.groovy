package org.osiam.test.integration

import org.osiam.client.oauth.AccessToken
import org.osiam.client.update.UpdateGroup
import org.osiam.resources.scim.Email
import org.osiam.resources.scim.Group
import org.osiam.resources.scim.GroupRef
import org.osiam.resources.scim.MemberRef
import org.osiam.resources.scim.Meta
import org.osiam.resources.scim.User

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
        def group = new Group.Builder("testGroup").build()
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
        def parentGroup = new Group.Builder("parentGroup").build()
        def memberGroup = new Group.Builder("memberGroup").build()
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

    def 'remove member from group'() {
        given:
		AccessToken accessToken = osiamConnector.retrieveAccessToken()
        def memberGroup1 = osiamConnector.createGroup(new Group.Builder('memberGroup1').build(), accessToken)
        def memberGroup2 = osiamConnector.createGroup(new Group.Builder('memberGroup2').build(), accessToken)
        def memberUser = osiamConnector.createUser(new User.Builder('userMember').setPassword('test').build(), accessToken)

        def groupMember1 = new MemberRef.Builder().setValue(memberGroup1.getId()).build()
        def groupMember2 = new MemberRef.Builder().setValue(memberGroup2.getId()).build()
        def userMember3 = new MemberRef.Builder().setValue(memberUser.getId()).build()
        def parentGroup = new Group.Builder('parent').setMembers([groupMember1, groupMember2, userMember3] as Set).build()

        def retParentGroup = osiamConnector.createGroup(parentGroup, accessToken)


        def updateGroup = new Group.Builder()
                .setMembers([new MemberRef.Builder(memberGroup1).setOperation('delete').build()] as Set)
                .build()

        when:
        def resultParentGroup = osiamConnector.updateGroup(retParentGroup.getId(), updateGroup, accessToken)

        then:
        parentGroup.getMembers().size() == 3
        resultParentGroup.getMembers().size() == 2
        def persistedParent = osiamConnector.getGroup(retParentGroup.getId(), accessToken)
        persistedParent.getMembers().size() == 2
    }

    def 'remove all members from group'() {
        given:
		
		AccessToken accessToken = osiamConnector.retrieveAccessToken()
		
        def memberGroup1 = osiamConnector.createGroup(new Group.Builder('memberGroup10').build(), accessToken)
        def memberGroup2 = osiamConnector.createGroup(new Group.Builder('memberGroup20').build(), accessToken)

        def member1 = new MemberRef.Builder().setValue(memberGroup1.getId()).build()
        def member2 = new MemberRef.Builder().setValue(memberGroup2.getId()).build()
        def parentGroup = new Group.Builder('parent1').setMembers([member1, member2] as Set).build()

        def parent = osiamConnector.createGroup(parentGroup, accessToken)

        def updateGroup = new Group.Builder()
                .setMeta(new Meta.Builder(null, null).setAttributes(['members'] as Set).build())
                .build()

        when:
        def result = osiamConnector.updateGroup(parent.getId(), updateGroup, accessToken)

        then:
        parent.getMembers().size() == 2
        result.getMembers().isEmpty()
        def persistedParent = osiamConnector.getGroup(parent.getId(), accessToken)
        persistedParent.getMembers().isEmpty()
    }

    def "ignoring required and read only attributes"(){
        given:
        def group = new Group.Builder("Group").build()

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