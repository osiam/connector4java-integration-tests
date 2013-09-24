package org.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class GroupServiceIT extends AbstractIntegrationTestBase {

    private static final String EXPECTED_GROUPNAME = "test_group01";
	static final private String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
	static final private String EXPECTED_CREATED_DATE = "2013-07-31 21:43:18";
    private String idStandardGroup;
    private Date created;
        	
    @Before
    public void setUp() throws Exception {
        created = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(EXPECTED_CREATED_DATE);
    }

    @Test
    public void ensure_all_values_are_deserialized_correctly() throws Exception {
        given_a_test_group_ID();
        Group actualGroup = oConnector.getGroup(idStandardGroup, accessToken);
        
        assertEquals(created, actualGroup.getMeta().getCreated());
        assertEquals(created, actualGroup.getMeta().getLastModified());
        assertEquals(VALID_GROUP_ID, actualGroup.getId());
        assertEquals(EXPECTED_GROUPNAME, actualGroup.getDisplayName());
        Set<MultiValuedAttribute> users = actualGroup.getMembers();
        int memberCount = 0;
        for (MultiValuedAttribute multiValuedAttribute : users) {
            String userId = (String) multiValuedAttribute.getValue();
            assertEquals(VALID_USER_ID, userId);
            memberCount++;
        }
        assertEquals(1, memberCount);
    }

    @Test(expected = NoResultException.class)
    public void get_an_invalid_group_raises_exception() throws Exception {
        oConnector.getGroup(INVALID_ID, accessToken);
    }
    
    @Test(expected = UnauthorizedException.class)
    public void access_token_is_expired() throws Exception {
    	given_a_test_group_ID();
    	givenAnAccessTokenForOneSecond();
    	Thread.sleep(1000);
        oConnector.getGroup(INVALID_ID, accessToken);
        fail();
    }
    
    private void given_a_test_group_ID() {
        idStandardGroup = VALID_GROUP_ID;
    }
    
}
