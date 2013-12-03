package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.NoResultException;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MemberRef;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed_groups.xml")
@DatabaseTearDown(value = "/database_tear_down.xml", type = DatabaseOperation.DELETE_ALL)
public class GroupServiceIT extends AbstractIntegrationTestBase {

    private static final String EXPECTED_GROUPNAME = "test_group01";
    static final private String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
    static final private String EXPECTED_CREATED_DATE = "2013-07-31 21:43:18";
    private Date created;

    @Before
    public void setUp() throws Exception {
        created = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(EXPECTED_CREATED_DATE);
    }

    @Test
    public void all_members_are_transmitted() {
        Group group = oConnector.getGroup(VALID_GROUP_ID, accessToken);

        Set<MemberRef> members = group.getMembers();

        assertThat(members, hasSize(1));

    }

    @Test
    public void group_member_is_the_expected_one() {
        Group group = oConnector.getGroup(VALID_GROUP_ID, accessToken);

        for (MultiValuedAttribute actMember : group.getMembers()) {
            assertThat(actMember.getValue(), is(equalTo(VALID_USER_ID)));
        }
    }

    @Test
    public void ensure_all_values_are_deserialized_correctly() throws Exception {
        Group group = oConnector.getGroup(VALID_GROUP_ID, accessToken);

        assertThat(group.getId(), is(equalTo(VALID_GROUP_ID)));
        assertThat(group.getMeta().getCreated(), is(equalTo(created)));
        assertThat(group.getMeta().getLastModified(), is(equalTo(created)));
        assertThat(group.getDisplayName(), is(equalTo(EXPECTED_GROUPNAME)));
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_group_raises_exception() throws Exception {
        oConnector.getGroup(INVALID_ID, accessToken);
        fail("Exception expected");
    }

}
