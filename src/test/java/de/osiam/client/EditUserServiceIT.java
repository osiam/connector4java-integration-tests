package de.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.OsiamUserService;
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

import java.util.*;

import static junit.framework.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class EditUserServiceIT extends AbstractIntegrationTestBase{

    private OsiamUserService service;
    private UUID validUUID = null;

    @Before
    public void setUp() throws Exception {
        service = new OsiamUserService.Builder(endpointAddress).build();
    }

    @Test (expected = ConflictException.class)
    public void create_user_with_no_username_raises_exception(){
        User newUser = new User.Builder().build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_user_with_exisitng_username_raises_exception(){
        User newUser = new User.Builder("hsimpson").build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test (expected = ConflictException.class)
    public void create_empty_user_raises_exception(){
        User newUser = new User.Builder("").build();
        service.createUser(newUser, accessToken);
        fail("Exception excpected");
    }

    @Test
    public void create_simple_User(){
        User newUser = new User.Builder("csu").build();
        User savedUser = service.createUser(newUser, accessToken);
        assertTrue(savedUser.getId().length() > 0);
        User dbUSer = service.getUserByUUID(UUID.fromString(savedUser.getId()), accessToken);
        assertEquals(newUser.getUserName(), dbUSer.getUserName());
    }

    @Test
    public void create_user_with_existing_uuid(){
        String hSimpsonId = "7d33bcbe-a54c-43d8-867e-f6146164941e";
        User newUser = new User.Builder("crweu").setId(hSimpsonId).build();
        service.createUser(newUser, accessToken);
        User dbUser = service.getUserByUUID(UUID.fromString(hSimpsonId), accessToken);

        assertEquals("hsimpson", dbUser.getUserName());
    }

    @Test
    public void given_uuid_to_new_user_has_changed_after_saving()
    {
        String userId = "1d33bcbe-a54c-43d8-867e-f6146164941e";
        User newUser = new User.Builder("gutnuhcas").setId(userId).build();
        User savedUser = service.createUser(newUser, accessToken);

        assertNotSame(userId, savedUser.getId());
    }

    @Test
    public void created_user_can_be_found(){
        String userName = "cucbf";

        Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(userName))).build();
        QueryResult<User> result = service.searchUsers(query, accessToken);
        assertEquals(0, result.getResources().size());

        User newUser = new User.Builder(userName).build();
        service.createUser(newUser, accessToken);

        result = service.searchUsers(query, accessToken);
        assertEquals(1, result.getResources().size());
        User dbUser = result.getResources().get(0);
        assertNotSame(userName, dbUser.getUserName());
    }

    @Test
    public void uuid_return_user_same_as_new_loaded_uuid(){
        String userId = "1d33bcbe-a54c-43d8-867e-f6146164941e";
        String userName = "gutnuhcas";
        User newUser = new User.Builder(userName).setId(userId).build();
        User savedUser = service.createUser(newUser, accessToken);
        Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(userName))).build();
        QueryResult<User> result = service.searchUsers(query, accessToken);

        assertEquals(1, result.getResources().size());
        User dbUser = result.getResources().get(0);
        assertEquals(savedUser.getId(), dbUser.getId());
    }

    @Test
    public void create_complete_user(){

        String uuid = "";
        try{
            User newUser = createCompleteUser();
            User savedUser = service.createUser(newUser, accessToken);
            uuid = savedUser.getId();
            Query query = new Query.Builder(User.class).filter(new Query.Filter(User.class).startsWith(User_.userName.equalTo(newUser.getUserName()))).build();
            QueryResult<User> result = service.searchUsers(query, accessToken);

            assertEquals(1, result.getResources().size());
            User dbUser = result.getResources().get(0);
            assertEquals(savedUser.getId(), dbUser.getId());
            assertEqualsUser(newUser, dbUser);
      }finally {
            if(uuid.length() > 0){
                service.deleteUserByUUID(UUID.fromString(uuid), accessToken);
            }
      }
    }
    
    @Test
    public void user_is_deleted() throws Exception {
    	whenUserIsOnServer();
    	givenAValidUserUUIDForDeletion();
        whenUserIsDeleted();
        thenUserIsRemoveFromServer();
    }
    
    @Test (expected = NoResultException.class)
    public void group_is_not_deleted() throws Exception {
    	givenAValidGroupUUIDForDeletion();
        whenGroupIsDeleted();
        fail();
    }
    
    @Test (expected = NoResultException.class)
    public void delete_user_two_times() throws Exception {
    	whenUserIsOnServer();
    	givenAValidUserUUIDForDeletion();
        whenUserIsDeleted();
        thenUserIsRemoveFromServer();
        whenUserIsDeleted();
        fail();
    }
    
    @Test(expected = UnauthorizedException.class)
    public void provide_an_invalid_access_token_raises_exception() throws Exception {
    	givenAValidUserUUIDForDeletion();
        givenAnInvalidAccessToken();
        whenUserIsDeleted();
        fail();
    }

    private User createCompleteUser(){
        User user = null;

        Set<Object> any = new HashSet<Object>(Arrays.asList("anyStatement"));
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
        MultiValuedAttribute email01 = new MultiValuedAttribute.Builder().setValue("example@example.de").setPrimary(true).setType("email").build();
        MultiValuedAttribute email02 = new MultiValuedAttribute.Builder().setValue("example02@example.de").setPrimary(false).setType("email").build();
        List<MultiValuedAttribute> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);
        Name name = new Name.Builder().setFamilyName("familyName")
                .setGivenName("vorName")
                .setMiddleName("middle")
                .setFormatted("complete Name")
                .setHonorificPrefix("HPre")
                .setHonorificSuffix("HSu").build();

        user = new User.Builder("completeU")
                .setPassword("password")
                .setActive(true)
                .setAny(any)
                .setAddresses(addresses)
                .setLocale("de")
                .setName(name)
                .setNickName("aNicknane")
                .setTitle("Dr.")
                //.setEmails(emails) //TODO Die emails haben noch irgendeinen Fehler im Aufbau
                .build();

        return user;
    }

    private void assertEqualsUser(User expected, User actual){
        assertEquals(expected.getUserName(), actual.getUserName());
        assertEquals(expected.isActive(), actual.isActive());
        assertEquals(expected.getAny().size(), actual.getAny().size());
        assertEquals(expected.getAny(), actual.getAny());
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
            MultiValuedAttribute actualAttribute = actualMultiValuedAttributes.get(count);

            assertEquals(expectedAttribute.getDisplay(), actualAttribute.getDisplay());
            assertEquals(expectedAttribute.getOperation(), actualAttribute.getOperation());
            assertEquals(expectedAttribute.getType(), actualAttribute.getType());
            assertEquals(expectedAttribute.getValue(), actualAttribute.getValue());
        }

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
    
    private void whenUserIsOnServer() {
    	validUUID = UUID.fromString(DELETE_USER_UUID);
    }
    
    private void whenUserIsDeleted() {
        service.deleteUserByUUID(validUUID, accessToken);
    }
    
    private void givenAValidUserUUIDForDeletion() throws Exception {
        validUUID = UUID.fromString(DELETE_USER_UUID);
    }
    
    private void givenAValidGroupUUIDForDeletion() throws Exception {
        validUUID = UUID.fromString(VALID_GROUP_UUID);
    }
    
    private void whenGroupIsDeleted() {
        service.deleteUserByUUID(validUUID, accessToken);
    }
    
    private void thenUserIsRemoveFromServer() {
    	try {
    		service.getUserByUUID(validUUID, accessToken);
    	} catch(NoResultException e) {
    		return;
    	} catch(Exception e) {
    		fail(Arrays.toString(e.getStackTrace()));
    	}
    	fail();
    }

}
