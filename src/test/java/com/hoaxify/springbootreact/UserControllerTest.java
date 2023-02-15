package com.hoaxify.springbootreact;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.hoaxify.springbootreact.configuration.AppConfiguration;
import com.hoaxify.springbootreact.exception.ApiException;
import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.model.UserUpdateVM;
import com.hoaxify.springbootreact.model.UserVM;
import com.hoaxify.springbootreact.service.UserService;
import com.hoaxify.springbootreact.service.impl.UserRepository;
import com.hoaxify.springbootreact.shared.GenericResponse;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserControllerTest {

	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	@Autowired
	AppConfiguration appConfiguration;
	
	private static final String API_V1_0_USERS = "/api/v1.0/users";
	
	@Before
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}
	
	@Test
	public void postUser_whenUserIsValid_receiveOk() {
		
		User user = TestUtil.createValidUser();
		
		//Not interested in returned responseType therefore use Object.class for last paramter
		//Only cared if its 200(Ok) or not
		ResponseEntity<Object> response = testRestTemplate.postForEntity(API_V1_0_USERS, user, Object.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	
	@Test
	public void postUser_whenUserIsValid_userSavedToDatabase() {
		User user = TestUtil.createValidUser();
		testRestTemplate.postForEntity(API_V1_0_USERS, user, Object.class);
		
		//Can failed as the number of data(1) in users table 
		//increased everytime a test is rerun
		assertThat(userRepository.count()).isEqualTo(1);
		
	}
	
	@Test
	public void postUser_whenUserIsValid_receiveSuccessMessage() {
		
		User user = TestUtil.createValidUser();
		
		ResponseEntity<GenericResponse> response = postSignup(user, GenericResponse.class);

		assertThat(response.getBody().getMessage()).isNotNull();
	}

	@Test
	public void postUser_whenUserIsValid_passwordIsHashedInDB() {
		User user = TestUtil.createValidUser();
		testRestTemplate.postForEntity(API_V1_0_USERS, user, Object.class);
		
		List<User> users = userRepository.findAll();
		User userInDB = users.get(0);
		assertThat(userInDB.getPassword()).isNotEqualTo(user.getPassword());
	}
	
	@Test
	public void postUser_whenUserHasNullUsername_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		//Failed: Not receive Bad Request since it not check if username is null
		//Bad Request: 400
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullDisplayName_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setUsername("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasDisplayNameWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setDisplayName("abc");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void postUser_whenUserHasPasswordWithLessThanRequired_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("P4sswor");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasUsernameExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setUsername(valueOf256Chars);
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		//Failed  
		//500: Bad Request (Valid request, but column is limited to 255 therefore DB ejected request).
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordExceedsTheLengthLimit_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		String valueOf256Chars = IntStream.rangeClosed(1,256).mapToObj(x -> "a").collect(Collectors.joining());
		user.setPassword(valueOf256Chars + "A1");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllLowercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("alllowercase");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllUppercase_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("ALLUPPERCASE");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserHasPasswordWithAllNumber_receiveBadRequest() {
		User user = TestUtil.createValidUser();
		user.setPassword("12345");
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void postUser_whenUserIsInvalid_receiveApiException() {
		User user = new User();
		ResponseEntity<ApiException> response = postSignup(user, ApiException.class);
		
		//Failed because Spring send back default JSON response
		//We need to receive API error object by handling @Valid error and generating custom object as response
		assertThat(response.getBody().getUrl()).isEqualTo(API_V1_0_USERS);
	}
	
	@Test
	public void postUser_whenUserIsInvalid_receiveApiExceptionWithValidationError() {
		User user = new User();
		ResponseEntity<ApiException> response = postSignup(user, ApiException.class);
		
		assertThat(response.getBody().getValidationErrors().size()).isEqualTo(3);
	}
	
	@Test
	public void postUser_whenUserHasNullUsername_receiveMessageOfNullErrorForUsername() {
		User user = TestUtil.createValidUser();
		user.setUsername(null);	
		ResponseEntity<ApiException> response = postSignup(user, ApiException.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
			
		//Failed: Not custom error message
		assertThat(validationErrors.get("username")).isEqualTo("Username cannot be null");
	}
	
	@Test
	public void postUser_whenUserHasNullPassword_receiveGenericMessageOfNullError() {
		User user = TestUtil.createValidUser();
		user.setPassword(null);	
		ResponseEntity<ApiException> response = postSignup(user, ApiException.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
			
		assertThat(validationErrors.get("password")).isEqualTo("Cannot be null");
	}
	
	@Test
	public void postUser_whenAnotherUserHasSameUsername_receiveBadRequest() {
		userRepository.save(TestUtil.createValidUser());
		
		User user = TestUtil.createValidUser();
		ResponseEntity<Object> response = postSignup(user, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void getUser_whenNoUserInDB_receiveOK() {
		ResponseEntity<Object> response = testRestTemplate.getForEntity(API_V1_0_USERS, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getUser_whenNoUserInDB_receivePageWithZeroItem() {
		
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		
		assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(0);
	}
	
	@Test
	public void getUser_whenUserExistInDB_receivePageWithUser() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Object>> response = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		assertThat(response.getBody().getNumberOfElements()).isEqualTo(1);
	}
	
	@Test
	public void getUser_whenUserExistInDB_receiveUserWithoutPassword() {
		userRepository.save(TestUtil.createValidUser());
		ResponseEntity<TestPage<Map<String, Object>>> response = getUsers(new ParameterizedTypeReference<TestPage<Map<String, Object>>>() {});
		
		Map<String, Object> entity = response.getBody().getContent().get(0);
		assertThat(entity.containsKey("password")).isFalse();
	}
	
	@Test
	public void getUser_whenPageIsRequestedFor3ItemsPerPateWhereDBHas20Users_receive3Users() {
		//Looping interger value
		IntStream.rangeClosed(1, 20).mapToObj(i -> "text-user-" + i)
		.map(username -> TestUtil.createValidUser(username))
		.forEach(user -> userRepository.save(user));
		
		String path = API_V1_0_USERS + "?page=0&size=3";
		ResponseEntity<TestPage<Object>> response = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
		
		assertThat(response.getBody().getContent().size()).isEqualTo(3);
	}
	
	@Test
	public void getUser_whenPageSizeNotProvided_receivePageSizeAs10() {
		
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		
		//Failed because default value in property file is 20
		assertThat(responseEntity.getBody().getSize()).isEqualTo(10);
	}
	
	@Test
	public void getUser_whenPageSizeGreaterThan100_receivePageSizeAs100() {
		
		String path = API_V1_0_USERS + "?size=500";
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
		
		//Failed because default value in property file is 2000
		assertThat(responseEntity.getBody().getSize()).isEqualTo(100);
	}
	
	@Test
	public void getUser_whenPageSizeIsNegative_receivePageSizeAs10() {
		//No need do anything for configuration
		String path = API_V1_0_USERS + "?size=-5";
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
		
		assertThat(responseEntity.getBody().getSize()).isEqualTo(10);
	}
	
	@Test
	public void getUser_whenPageIsNegative_receiveFirstPage() {
		//No need do anything for configuration
		String path = API_V1_0_USERS + "?page=-5";
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(path, new ParameterizedTypeReference<TestPage<Object>>() {});
		
		assertThat(responseEntity.getBody().getNumber()).isEqualTo(0);
	}
	
	@Test
	public void getUser_whenUserLoggedIn_receivePageWithoutLoggedInUser() {
		//Make sure logged in user not show in listing
		
		userService.saveUser(TestUtil.createValidUser("user1"));
		userService.saveUser(TestUtil.createValidUser("user2"));
		userService.saveUser(TestUtil.createValidUser("user3"));
		authenticate("user1");
		
		ResponseEntity<TestPage<Object>> responseEntity = getUsers(new ParameterizedTypeReference<TestPage<Object>>() {});
		
		assertThat(responseEntity.getBody().getTotalElements()).isEqualTo(2);
	}
	
	@Test
	public void getUserByUsername_whenUserExist_receiveOk() {
		String username = "text-user";
		userService.saveUser(TestUtil.createValidUser(username));
		
		ResponseEntity<Object> response =  getUser(username, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void getUserByUsername_whenUserExist_receiveUserWithoutPassword() {
		String username = "text-user";
		userService.saveUser(TestUtil.createValidUser(username));
		
		ResponseEntity<String> response =  getUser(username, String.class);
		assertThat(response.getBody().contains("password")).isFalse();
	}
	
	@Test
	public void getUserByUsername_whenUserNotExist_receiveNotFound() {
		ResponseEntity<Object> response =  getUser("Unknown", Object.class);
		
		//Failed 500: Because convert Null object into UserVM which trigger NullPointerException
		//Create custom exception - NotFoundException
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
	
	@Test
	public void getUserByUsername_whenUserNotExist_receiveApiException() {
		ResponseEntity<ApiException> response =  getUser("Unknown", ApiException.class);
		assertThat(response.getBody().getMessage().contains("Unknown")).isTrue();
	}
	
	@Test
	public void putUser_whenUnauthorizedUserSendsRequest_receiveUnauthorized() {	
		ResponseEntity<Object> response = putUser(123, null, Object.class);
		
		//Failed 405 Method not allowed: As same path is used for getUserByUsername
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void putUser_whenAuthorizedUserSendsUpdateToAnotherUser_receiveForbidden() {	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		
		long newUserID = user.getId() + 123;
		ResponseEntity<Object> response = putUser(newUserID, null, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
	
	@Test
	public void putUser_whenUnauthorizedUserSendsRequest_receiveApiException() {	
		ResponseEntity<ApiException> response = putUser(123, null, ApiException.class);
		assertThat(response.getBody().getUrl()).contains("users/123");
	}
	
	@Test
	public void putUser_whenAuthorizedUserSendsUpdateToAnotherUser_receiveApiException() {	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		
		long newUserID = user.getId() + 123;
		ResponseEntity<ApiException> response = putUser(newUserID, null, ApiException.class);
		
		assertThat(response.getBody().getUrl()).contains("users/" + newUserID);
	}
	
	@Test
	public void putUser_whenValidRequestBodyFromAuthourizedUser_receiveOk() {	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		
		//Not allow user to change username and password
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		//Wrap the UerUpdateVM as request entity
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void putUser_whenValidRequestBodyFromAuthourizedUser_receiveUserVMWithUpdatedDisplayName() {	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		assertThat(response.getBody().getDisplayName()).isEqualTo(updatedUser.getDisplayName());
	}

	@Test
	public void putUser_witValidRequestBodyWithSupportedImageFromAuthorizedUser_receiveUserVMWithRandomImageName() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		
		//Uploading image: Convert image file to base64 
		//					OR send multiple part body request instead of JSON request
		//1st option: Convert image file to base64
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		String imageString = readFileToBase64("profile.png");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		assertThat(response.getBody().getImage()).isNotEqualTo("profile-image.png");
	}
	
	@Test
	public void putUser_withValidRequestBodyWithSupportedImageFromAuthorizedUser_imageStoredUnderProfileFolder() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("profile.png");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		
		String storedImageName = response.getBody().getImage();
		String profilePicturePath = appConfiguration.getFullProfileImagePath() + "/" + storedImageName;
		
		System.out.println("The file path is: " + profilePicturePath);
		File storedImage = new File(profilePicturePath);
			
		assertThat(storedImage.exists()).isTrue();
	}

	@Test
	public void putUser_whenInValidRequestBodyWithNullDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = new UserUpdateVM();
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
			
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void putUser_whenInValidRequestBodyWithLessThanMinDisplayNameFromAuthorizedUser_receiveBadRequest() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updatedUser =new UserUpdateVM();
		updatedUser.setDisplayName("abc");	
		
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
			
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void putUser_witValidRequestBodyWithJPGImageFromAuthorizedUser_receiveOk() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		String imageString = readFileToBase64("test-jpg.jpg");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void putUser_witValidRequestBodyWithGIFImageFromAuthorizedUser_receiveBadRequest() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		String imageString = readFileToBase64("test-gif.gif");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<Object> response = putUser(user.getId(), requestEntity, Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
	
	@Test
	public void putUser_witValidRequestBodyWithTXTImageFromAuthorizedUser_receiveValidationErrorForProfileImage() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());

		UserUpdateVM updatedUser = createValidUserUpdateVM();
		
		String imageString = readFileToBase64("test-txt.txt");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<ApiException> response = putUser(user.getId(), requestEntity, ApiException.class);
		Map<String, String> validationErrors = response.getBody().getValidationErrors();
		assertThat(validationErrors.get("image")).isEqualTo("Only PNG and JPG files are allowed");
	}
	
	@Test
	public void putUser_witValidRequestBodyWithJPGImageFromUserHaveImage_receiveOldImageFromStorage() throws IOException{	
		User user = userService.saveUser(TestUtil.createValidUser("user1"));
		authenticate(user.getUsername());
		UserUpdateVM updatedUser = createValidUserUpdateVM();
		String imageString = readFileToBase64("test-jpg.jpg");
		updatedUser.setImage(imageString);
			
		HttpEntity<UserUpdateVM> requestEntity = new HttpEntity<>(updatedUser);
		ResponseEntity<UserVM> response = putUser(user.getId(), requestEntity, UserVM.class);
		
		//New File upload
		putUser(user.getId(), requestEntity, UserVM.class);
		
		//Get old image file with old name
		String storedImageName = response.getBody().getImage();
		String profilePicturePath = appConfiguration.getFullProfileImagePath() + "/" + storedImageName;
		File storedImage = new File(profilePicturePath);
		
		assertThat(storedImage.exists()).isFalse();
	}
	
	private String readFileToBase64(String fileName) throws IOException {
		//Load Picture
		ClassPathResource imageResource = new ClassPathResource(fileName);
		
		//Convert profile pic to string with Apache Common IO library
		byte[] imageArr = FileUtils.readFileToByteArray(imageResource.getFile());
		String imageString = Base64.getEncoder().encodeToString(imageArr);
		
		return imageString;
	}
	
	private UserUpdateVM createValidUserUpdateVM() {
		UserUpdateVM updatedUser = new UserUpdateVM();
		updatedUser.setDisplayName("newDisplayName");
		return updatedUser;
	}
	
	private void authenticate(String username) {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor(username, "P4ssword"));
	}
	
	public <T> ResponseEntity<T> postSignup(Object request, Class<T> response){
		return testRestTemplate.postForEntity(API_V1_0_USERS, request, response);
	}
	
	public <T> ResponseEntity<T> getUsers(ParameterizedTypeReference<T> responseType){
		return testRestTemplate.exchange(API_V1_0_USERS, HttpMethod.GET, null, responseType);
	}
	
	public <T> ResponseEntity<T> getUsers(String path, ParameterizedTypeReference<T> responseType){
		return testRestTemplate.exchange(path, HttpMethod.GET, null, responseType);
	}
	
	public <T> ResponseEntity<T> getUser(String username, Class<T> responseType){
		String path = API_V1_0_USERS + "/" + username;
		return testRestTemplate.getForEntity(path, responseType);
	}
	
	public <T> ResponseEntity<T> putUser(long id, HttpEntity<?> requestEntity, Class<T> responseType){
		String path = API_V1_0_USERS + "/" + id;
		
		//Need requestEntity as sending body obj
		return testRestTemplate.exchange(path, HttpMethod.PUT, requestEntity, responseType);
	}
	
	@After
	public void cleanDirectory() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagePath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
	}

}
