package org.osiam.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ForbiddenException;
import org.osiam.client.oauth.AccessToken;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.client.query.Query;
import org.osiam.client.update.UpdateGroup;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.Group;
import org.osiam.resources.scim.User;
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
public class ScopeIT {

    static final private String VALID_USER_ID = "834b410a-943b-4c80-817a-4465aed037bc";
    static final private String VALID_GROUP_ID = "69e1a5dc-89be-4343-976c-b5541af249f4";
	private static String ENDPOINT_ADDRESS = "http://localhost:8180/osiam-server";
    private static String CLIENT_ID = "example-client";
    private static String CLIENT_SECRET = "secret";
    private OsiamConnector oConnector;
    private AccessToken accessToken;
    private OsiamConnector.Builder oConBuilder;
    
    @Before
    public void setUp() throws Exception {

        oConBuilder = new OsiamConnector.Builder(ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala")
                ;
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_retrieve_user_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToGetUser();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_retrieve_group_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToGetGroup();
    }

    @Test (expected = ForbiddenException.class)
    public void try_to_retrieve_all_users_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToGetAllUsers();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_retrieve_all_groups_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToGetAllGroups();
    }
    
    @Test(expected = ForbiddenException.class)
    public void try_to_create_user_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	createTestUser();
    }
    
    @Test(expected = ForbiddenException.class)
    public void try_to_create_group_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	createTestGroup();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_get_actual_user_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToGetMe();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_update_user_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToUpdateUser();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_update_group_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToUpdateGroup();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_search_for_user_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToSearchForUsers();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_search_for_group_raises_exception(){
    	setInvalidScope();
    	retrieveAccessToken();
    	tryToSearchForGroups();
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_delete_user_raises_exception(){
    	setInvalidScopeForDeleting();
    	retrieveAccessToken();
    	oConnector.deleteUser(VALID_USER_ID, accessToken);
    }
    
    @Test (expected = ForbiddenException.class)
    public void try_to_delete_group_raises_exception(){
    	setInvalidScopeForDeleting();
    	retrieveAccessToken();
    	oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
    }
    
    @Test 
    public void try_to_retrieve_user(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToGetUser();
    }
    
    @Test 
    public void try_to_retrieve_group(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToGetGroup();
    }

    @Test 
    public void try_to_retrieve_all_users(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToGetAllUsers();
    }
    
    @Test 
    public void try_to_retrieve_all_groups(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToGetAllGroups();
    }
    
    @Test
    public void try_to_create_user(){
    	oConnector = oConBuilder.setScope(Scope.POST).build();
    	retrieveAccessToken();
    	createTestUser();
    }
    
    @Test
    public void try_to_create_group(){
    	oConnector = oConBuilder.setScope(Scope.POST).build();
    	retrieveAccessToken();
    	createTestGroup();
    }
    
    @Test 
    public void try_to_get_actual_user(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToGetMe();
    }
    
    @Test
    public void try_to_update_user(){
    	oConnector = oConBuilder.setScope(Scope.PATCH).build();
    	retrieveAccessToken();
    	tryToUpdateUser();
    }
    
    @Test
    public void try_to_update_group(){
    	oConnector = oConBuilder.setScope(Scope.PATCH).build();
    	retrieveAccessToken();
    	tryToUpdateGroup();
    }
    
    @Test
    public void try_to_search_for_user(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToSearchForUsers();
    }
    
    @Test 
    public void try_to_search_for_group(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    	retrieveAccessToken();
    	tryToSearchForGroups();
    }
    
    @Test 
    public void try_to_delete_user(){
    	oConnector = oConBuilder.setScope(Scope.DELETE, Scope.POST).build();
    	retrieveAccessToken();
    	String newUserId = createTestUser();
    	oConnector.deleteUser(newUserId, accessToken);
    }
    
    @Test 
    public void try_to_delete_group(){
    	oConnector = oConBuilder.setScope(Scope.DELETE).build();
    	retrieveAccessToken();
    	oConnector.deleteGroup(VALID_GROUP_ID, accessToken);
    }
    
    @Test 
    public void try_to_get_actual_user_with_string_scope(){
    	oConnector = oConBuilder.setScope("GET").build();
    	retrieveAccessToken();
    	tryToGetMe();
    }
    
    private String createTestUser(){
    	User user = new User.Builder("testUSer0065").build();
    	return oConnector.createUser(user, accessToken).getId();
    }
    
    private void createTestGroup(){
    	Group group = new Group.Builder().setDisplayName("test").build();
    	oConnector.createGroup(group, accessToken);
    }
    
    private void setInvalidScope(){
    	oConnector = oConBuilder.setScope(Scope.DELETE).build();
    }
    
    private void setInvalidScopeForDeleting(){
    	oConnector = oConBuilder.setScope(Scope.GET).build();
    }
    
    private void retrieveAccessToken(){
    	accessToken = oConnector.retrieveAccessToken();
    }
    
    private void tryToGetUser(){
    	oConnector.getUser(VALID_USER_ID, accessToken);
    }
    
    private void tryToGetGroup(){
    	oConnector.getGroup(VALID_GROUP_ID, accessToken);
    }
    
    private void tryToGetAllUsers(){
    	oConnector.getAllUsers(accessToken);
    }
    
    private void tryToGetAllGroups(){
    	oConnector.getAllGroups(accessToken);
    }
    
    private void tryToGetMe(){
    	oConnector.getMe(accessToken);
    }
    
    private void tryToUpdateUser(){
    	UpdateUser updateUser = new UpdateUser.Builder().updateUserName("newName").updateActive(false).build();
    	oConnector.updateUser(VALID_USER_ID, updateUser, accessToken);
    }
    
    private void tryToUpdateGroup(){
    	UpdateGroup updateGroup = new UpdateGroup.Builder().updateDisplayName("irrelevant").build();
    	oConnector.updateGroup(VALID_GROUP_ID, updateGroup, accessToken);
    }
    
    private void tryToSearchForUsers(){
    	Query query = new Query.Builder(User.class).setStartIndex(0).build();
    	oConnector.searchUsers(query, accessToken);
    }
    
    private void tryToSearchForGroups(){
    	Query query = new Query.Builder(Group.class).setStartIndex(0).build();
    	oConnector.searchUsers(query, accessToken);
    }
    
}
