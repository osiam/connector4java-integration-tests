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

import org.osiam.client.oauth.AccessToken
import org.osiam.resources.scim.Group
import org.osiam.resources.scim.MemberRef
import org.osiam.resources.scim.UpdateGroup
import org.osiam.resources.scim.User

/**
 * Test to ensure that updating group membership works.
 */
class UserGroupMembershipIT extends AbstractIT {

    def setup() {
        setupDatabase("database_seed_user_group_membership.xml")
    }

    def "add user member to group"(){
        given:
        def user = new User.Builder("testUser").setPassword("test").build()
        def group = new Group.Builder("testGroup").build()
        def createdUser = OSIAM_CONNECTOR.createUser(user, OSIAM_CONNECTOR.retrieveAccessToken())
        def createdGroup = OSIAM_CONNECTOR.createGroup(group, OSIAM_CONNECTOR.retrieveAccessToken())

        def updateGroup = new UpdateGroup.Builder()
                .addMember(createdUser.getId())
                .build()

        when:
        def updatedGroup = OSIAM_CONNECTOR.updateGroup(createdGroup.getId(), updateGroup, OSIAM_CONNECTOR.retrieveAccessToken())

        then:
        updatedGroup.getMembers().size() == 1

        def theUserWithGroup = OSIAM_CONNECTOR.getUser(createdUser.getId(), OSIAM_CONNECTOR.retrieveAccessToken())
        theUserWithGroup.getGroups().size() == 1

        def theGroupWithMembers = OSIAM_CONNECTOR.getGroup(createdGroup.getId(), OSIAM_CONNECTOR.retrieveAccessToken())
        theGroupWithMembers.getMembers().size() == 1
    }

    def "add group member to group"(){
        given:
        def parentGroup = new Group.Builder("parentGroup").build()
        def memberGroup = new Group.Builder("memberGroup").build()
        def createdParentGroup = OSIAM_CONNECTOR.createGroup(parentGroup, OSIAM_CONNECTOR.retrieveAccessToken())
        def createdMemberGroup = OSIAM_CONNECTOR.createGroup(memberGroup, OSIAM_CONNECTOR.retrieveAccessToken())

        def updateGroup = new UpdateGroup.Builder()
                .addMember(createdMemberGroup.getId())
                .build()

        when:
        def updatedGroup = OSIAM_CONNECTOR.updateGroup(createdParentGroup.getId(), updateGroup, OSIAM_CONNECTOR.retrieveAccessToken())

        then:
        updatedGroup.getMembers().size() == 1

        def parentGroupWithMembers = OSIAM_CONNECTOR.getGroup(createdParentGroup.getId(), OSIAM_CONNECTOR.retrieveAccessToken())
        parentGroupWithMembers.getMembers().size() == 1

        def memberGroupWithparent = OSIAM_CONNECTOR.getGroup(createdMemberGroup.getId(), OSIAM_CONNECTOR.retrieveAccessToken())
        memberGroupWithparent.getMembers().size() == 0
    }

    def 'remove member from group'() {
        given:
        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken()
        def memberGroup1 = OSIAM_CONNECTOR.createGroup(new Group.Builder('memberGroup1').build(), accessToken)
        def memberGroup2 = OSIAM_CONNECTOR.createGroup(new Group.Builder('memberGroup2').build(), accessToken)
        def memberUser = OSIAM_CONNECTOR.createUser(new User.Builder('userMember').setPassword('test').build(), accessToken)

        def groupMember1 = new MemberRef.Builder().setValue(memberGroup1.getId()).build()
        def groupMember2 = new MemberRef.Builder().setValue(memberGroup2.getId()).build()
        def userMember3 = new MemberRef.Builder().setValue(memberUser.getId()).build()
        def parentGroup = new Group.Builder('parent').setMembers([
            groupMember1,
            groupMember2,
            userMember3] as Set).build()

        def retParentGroup = OSIAM_CONNECTOR.createGroup(parentGroup, accessToken)

        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .deleteMember(memberGroup1.getId())
                .build()

        when:
        def resultParentGroup = OSIAM_CONNECTOR.updateGroup(retParentGroup.getId(), updateGroup, accessToken)

        then:
        parentGroup.getMembers().size() == 3
        resultParentGroup.getMembers().size() == 2
        def persistedParent = OSIAM_CONNECTOR.getGroup(retParentGroup.getId(), accessToken)
        persistedParent.getMembers().size() == 2
    }

    def 'remove all members from group'() {
        given:

        AccessToken accessToken = OSIAM_CONNECTOR.retrieveAccessToken()

        def memberGroup1 = OSIAM_CONNECTOR.createGroup(new Group.Builder('memberGroup10').build(), accessToken)
        def memberGroup2 = OSIAM_CONNECTOR.createGroup(new Group.Builder('memberGroup20').build(), accessToken)

        def member1 = new MemberRef.Builder().setValue(memberGroup1.getId()).build()
        def member2 = new MemberRef.Builder().setValue(memberGroup2.getId()).build()
        def parentGroup = new Group.Builder('parent1').setMembers([member1, member2] as Set).build()

        def parent = OSIAM_CONNECTOR.createGroup(parentGroup, accessToken)

        UpdateGroup updateGroup = new UpdateGroup.Builder()
                .deleteMembers()
                .build()

        when:
        def result = OSIAM_CONNECTOR.updateGroup(parent.getId(), updateGroup, accessToken)

        then:
        parent.getMembers().size() == 2
        result.getMembers().isEmpty()
        def persistedParent = OSIAM_CONNECTOR.getGroup(parent.getId(), accessToken)
        persistedParent.getMembers().isEmpty()
    }

}