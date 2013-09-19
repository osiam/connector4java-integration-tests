package de.osiam.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.exception.NoResultException;
import org.osiam.client.exception.UnauthorizedException;
import org.osiam.client.query.Query;
import org.osiam.client.query.QueryResult;
import org.osiam.client.query.metamodel.User_;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.MultiValuedAttribute;
import org.osiam.resources.scim.Name;
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
public class EditUserServiceIT extends AbstractIntegrationTestBase{

    private String validId = null;
    private static String ID_EXISTING_USER = "7d33bcbe-a54c-43d8-867e-f6146164941e";
    private String newId = UUID.randomUUID().toString();
    private String USER_NAME_EXISTING_USER = "hsimpson";
    private User NEW_USER;
    private User RETURN_USER;
    private User DB_USER;
    private static final String IRRELEVANT = "irrelevant";
    private Query QUERY;

    @Test (expected = ConflictException.class)
    public void create_user_with_no_username_raises_exception(){
        initializeUserWithNoUserName();
        createUser();
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_user_with_existing_username_raises_exception(){
        initializeUserWithExistingUserName();
        createUser();
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_empty_user_raises_exception(){
        initializeUserWithEmptyUserName();
        createUser();
        fail("Exception excpected");
    }

    @Test
    public void create_simple_User(){
        initializeSimpleUser();
        createUser();
        returnUserHasValidId();
        loadUser(RETURN_USER.getId());
        returnAndDbUserHaveSameUserName();
    }

    @Test
    public void create_user_with_existing_id(){
        initializeSimpleUserWithID(ID_EXISTING_USER.toString());
        createUser();
        loadUser(ID_EXISTING_USER);
        existingUserNameHasNotChanged();
    }

    @Test
    public void given_id_to_new_user_has_changed_after_saving(){
        initializeSimpleUserWithID(newId.toString());
        createUser();
        assertNotSame(newId.toString(), RETURN_USER.getId());
    }

    @Test
    public void created_user_can_be_found(){
        initialQueryToSearchUser();
        loadSingleUserByQuery();
        assertNull(DB_USER);

        initializeSimpleUser();
        createUser();
        loadSingleUserByQuery();
        assertNotNull(DB_USER);
        assertNotSame(IRRELEVANT, DB_USER.getUserName());
    }

    @Test
    public void id_return_user_same_as_new_loaded_id(){
        initializeSimpleUserWithID(newId.toString());
        createUser();
        initialQueryToSearchUser();
        loadSingleUserByQuery();
        assertNotNull(DB_USER);
        assertEquals(RETURN_USER.getId(), DB_USER.getId());
    }

    @Test
    public void create_complete_user(){

        try{
            buildCompleteUser();
            createUser();
            initialQueryToSearchUser();
            loadSingleUserByQuery();
            assertNotNull(DB_USER);
            assertEquals(RETURN_USER.getId(), DB_USER.getId());
            assertEqualsUser(NEW_USER, DB_USER);
      }finally {
            if(RETURN_USER != null){
                oConnector.deleteUser(RETURN_USER.getId(), accessToken);
            }
      }
    }
    
    @Test
    public void user_is_deleted() throws Exception {
    	givenAValidUserIDForDeletion();
        whenUserIsDeleted();
        thenUserIsRemoveFromServer();
    }
    
    
    @Test (expected = NoResultException.class)
    public void group_is_not_deleted() throws Exception {
    	givenAValidGroupIDForDeletion();
        whenGroupIsDeleted();
        fail();
    }
    
    @Test (expected = NoResultException.class)
    public void delete_user_two_times() throws Exception {
    	givenAValidUserIDForDeletion();
        whenUserIsDeleted();
        thenUserIsRemoveFromServer();
        whenUserIsDeleted();
        fail();
    }
    
    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
    	givenAValidUserIDForDeletion();
        givenAnInvalidAccessToken();
        whenUserIsDeleted();
        fail();
    }

    private void initializeUserWithNoUserName(){
        NEW_USER = new User.Builder().build();
    }

    private void initializeUserWithEmptyUserName(){
        NEW_USER = new User.Builder("").build();
    }

    private void initializeSimpleUser(){
        NEW_USER = new User.Builder(IRRELEVANT).build();
    }

    private void initializeSimpleUserWithID(String id){
        NEW_USER = new User.Builder(IRRELEVANT).setId(id).build();
    }

    private void initializeUserWithExistingUserName(){
        NEW_USER = new User.Builder(USER_NAME_EXISTING_USER).build();
    }

    private void returnUserHasValidId(){
        assertTrue(RETURN_USER.getId().length() > 0);
    }

    private void loadUser(String id){
        DB_USER = oConnector.getUser(id, accessToken);
    }

    private void loadSingleUserByQuery(){
        QueryResult<User> result = oConnector.searchUsers(QUERY, accessToken);
        if(result.getResources().size() == 0){
            DB_USER = null;
        }else if(result.getResources().size() == 1){
            DB_USER = result.getResources().get(0);
        }else{
             fail("No or one user should be found");
        }
    }

    private void existingUserNameHasNotChanged(){
        assertEquals(USER_NAME_EXISTING_USER, DB_USER.getUserName());
    }

    private void createUser(){
        RETURN_USER = oConnector.createUser(NEW_USER, accessToken);
    }

    private void initialQueryToSearchUser(){
        QUERY = new Query.Builder(User.class).setFilter(new Query.Filter(User.class, User_.userName.equalTo(IRRELEVANT))).build();
    }

    private void buildCompleteUser(){
        Address address = new Address.Builder()
                .setStreetAddress("Example Street 22")
                .setCountry("Germany")
                .setFormatted("Complete Adress")
                .setLocality("de")
                .setPostalCode("111111")
                .setRegion("Berlin")
                .build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(address);
        MultiValuedAttribute email01 = new MultiValuedAttribute.Builder().setValue("example@example.de").setPrimary(true).setType("work").build();
        MultiValuedAttribute email02 = new MultiValuedAttribute.Builder().setValue("example02@example.de").setPrimary(false).setType("home").build();
        List<MultiValuedAttribute> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);


        Name name = new Name.Builder().setFamilyName("familyName")
                .setGivenName("vorName")
                .setMiddleName("middle")
                .setFormatted("complete Name")
                .setHonorificPrefix("HPre")
                .setHonorificSuffix("HSu").build();

        NEW_USER = new User.Builder(IRRELEVANT)
                .setPassword("password")
                .setActive(true)
                .setAddresses(addresses)
                .setLocale("de")
                .setName(name)
                .setNickName("aNicknane")
                .setTitle("Dr.")
                .setEmails(emails)
                .build();
    }

    private void assertEqualsUser(User expected, User actual){
        assertEquals(expected.getUserName(), actual.getUserName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEqualsAddresses(expected.getAddresses(), actual.getAddresses());
        assertEquals(expected.getLocale(), actual.getLocale());
        assertEqualsName(expected.getName(), actual.getName());
        assertEquals(expected.getNickName(), actual.getNickName());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEqualsMultiValuedAttribute(expected.getEmails(), actual.getEmails());

    }

    private void assertEqualsMultiValuedAttribute(List<MultiValuedAttribute> expectedMultiValuedAttributes, List<MultiValuedAttribute> actualMultiValuedAttributes){
        if((expectedMultiValuedAttributes == null || expectedMultiValuedAttributes.size() == 0)
                && (actualMultiValuedAttributes == null || actualMultiValuedAttributes.size() == 0)){
            return;
        }
        assertEquals(expectedMultiValuedAttributes.size(), actualMultiValuedAttributes.size());
        for(int count = 0; count < expectedMultiValuedAttributes.size(); count++){
            MultiValuedAttribute expectedAttribute = expectedMultiValuedAttributes.get(count);
            MultiValuedAttribute actualAttribute = getMultiAttributeWithValue(actualMultiValuedAttributes, expectedAttribute.getValue().toString());
            if(actualAttribute == null){
                fail("MultiValueAttribute " + expectedAttribute.getValue() + " could not be found");
            }

            assertEquals(expectedAttribute.getDisplay(), actualAttribute.getDisplay());
            assertEquals(expectedAttribute.getOperation(), actualAttribute.getOperation());
            assertEquals(expectedAttribute.getType(), actualAttribute.getType());
            assertEquals(expectedAttribute.getValue(), actualAttribute.getValue());
        }

    }

    private MultiValuedAttribute getMultiAttributeWithValue(List<MultiValuedAttribute> multiValuedAttributes, String expectedValue){
        MultiValuedAttribute mutliVal = null;
        for(MultiValuedAttribute actAttribute : multiValuedAttributes){
            if(actAttribute.getValue().toString().equals(expectedValue)){
                mutliVal = actAttribute;
                break;
            }
        }
        return mutliVal;
    }

    private void assertEqualsName(Name expectedName, Name actualName){
        assertEquals(expectedName.getFamilyName(), actualName.getFamilyName());
        assertEquals(expectedName.getFormatted(), actualName.getFormatted());
        assertEquals(expectedName.getGivenName(), actualName.getGivenName());
        assertEquals(expectedName.getHonorificPrefix(), actualName.getHonorificPrefix());
        assertEquals(expectedName.getHonorificSuffix(), actualName.getHonorificSuffix());
        assertEquals(expectedName.getMiddleName(), actualName.getMiddleName());
    }

    private void assertEqualsAddresses(List<Address> expectedAddresses, List<Address> actualAddresses){
        assertEquals(expectedAddresses.size(), actualAddresses.size());
        for(int count = 0; count < expectedAddresses.size(); count++){
            Address expectedAddress = expectedAddresses.get(count);
            Address actualAddress = actualAddresses.get(count);

            assertEquals(expectedAddress.getCountry(), actualAddress.getCountry());
            assertEquals(expectedAddress.getFormatted(), actualAddress.getFormatted());
            assertEquals(expectedAddress.getLocality(), actualAddress.getLocality());
            assertEquals(expectedAddress.getPostalCode(), actualAddress.getPostalCode());
            assertEquals(expectedAddress.getRegion(), actualAddress.getRegion());
            assertEquals(expectedAddress.getStreetAddress(), actualAddress.getStreetAddress());
        }
    }
    
    private void whenUserIsDeleted() {
        oConnector.deleteUser(validId, accessToken);
    }
    
    private void givenAValidUserIDForDeletion() throws Exception {
        validId = DELETE_USER_ID;
    }
    
    private void givenAValidGroupIDForDeletion() throws Exception {
        validId = VALID_GROUP_ID;
    }
    
    private void whenGroupIsDeleted() {
        oConnector.deleteUser(validId, accessToken);
    }

    private void returnAndDbUserHaveSameUserName(){
        assertEquals(NEW_USER.getUserName(), DB_USER.getUserName());
    }
    
    private void thenUserIsRemoveFromServer() {
    	try {
            oConnector.getUser(validId, accessToken);
    	} catch(NoResultException e) {
    		return;
    	} catch(Exception e) {
    		fail(Arrays.toString(e.getStackTrace()));
    	}
    	fail();
    }

}
