package com.hoaxify.springbootreact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

import java.util.Map;

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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.hoaxify.springbootreact.exception.ApiException;
import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.service.UserService;
import com.hoaxify.springbootreact.service.impl.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LoginControllerTest {

	private static final String API_1_0_LOGIN = "/api/v1.0/login";
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	private User createValidUser() {
		//Posting User object to server with HTTP client		
		User user = new User();
		user.setUsername("test-user");
		user.setDisplayName("test-display");
		user.setPassword("P4ssword");
		user.setImage("profile-image.png");
		return user;
	}
	
	@Before
	public void cleanUp() {
		userRepository.deleteAll();
		//Start with clean request form to clear authentication header
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}
	
	@Test
	public void postLogin_withoutUserCredentials_receiveUnauthorized() {
		//401: Unauthorized
		ResponseEntity<Object> response = login(Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	@Test
	public void postLogin_withIncorrectCredentials_receiveUnauthorized() {
		authenticate();
		
		ResponseEntity<Object> response = login(Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	
	//Convert Error Object into API Exception
	@Test
	public void postLogin_withoutUserCredentials_receiveAPIError() {
		ResponseEntity<ApiException> response = login(ApiException.class);
		
		assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
	}
	
	//Receive API Exception without Validation Errors
	@Test
	public void postLogin_withoutUserCredentials_receiveApiExceptionWithoutValidationErrors() {
		ResponseEntity<String> response = login(String.class);
		
		//Failed because automatically added into validationErrors hashmap as field without value
		//Jackson provide option to exclude null field when converting object to JSON with @JsonInclude
		assertThat(response.getBody().contains("validationErrors")).isFalse();
	}
	
	@Test
	public void postLogin_withIncorrectCredentials_receiveUnauthorizedWithoutWWWAuthenticationHeader() {
		authenticate();
		
		ResponseEntity<Object> response = login(Object.class);
		
		assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
	}
	
	@Test
	public void postLogin_withValidCredential_receiveOk() {
		userService.saveUser(createValidUser());
		authenticate();
		ResponseEntity<Object> response = login(Object.class);
		
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	
	@Test
	public void postLogin_withValidCredential_receiveLoggedInUserId() {
		User userInDB = userService.saveUser(createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login2(new ParameterizedTypeReference<Map<String, Object>>(){});
		
		Map<String, Object> body = response.getBody();
		Integer id = (Integer) body.get("id");
		
		assertThat(id).isEqualTo((int) (long)userInDB.getId());
	}
	
	@Test
	public void postLogin_withValidCredential_receiveLoggedInUserImage() {
		User userInDB = userService.saveUser(createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login2(new ParameterizedTypeReference<Map<String, Object>>(){});
		
		Map<String, Object> body = response.getBody();
		String image = (String) body.get("image");
		
		assertThat(image).isEqualTo(userInDB.getImage());
	}
	
	@Test
	public void postLogin_withValidCredential_receiveLoggedInUserDisplayName() {
		User userInDB = userService.saveUser(createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login2(new ParameterizedTypeReference<Map<String, Object>>(){});
		
		Map<String, Object> body = response.getBody();
		String displayName = (String) body.get("displayName");
		
		assertThat(displayName).isEqualTo(userInDB.getDisplayName());
	}
	
	//Ignore for this application
//	@Test
//	public void postLogin_withValidCredential_receiveLoggedInUserPassword() {
//		userService.saveUser(createValidUser());
//		authenticate();
//		ResponseEntity<Map<String, Object>> response = login2(new ParameterizedTypeReference<Map<String, Object>>(){});
//		
//		Map<String, Object> body = response.getBody();
//		
//		//To ensure the response body dont have password
//		assertThat(body.containsKey("password")).isFalse();
//	}
	
	
	private void authenticate() {
		testRestTemplate.getRestTemplate().getInterceptors().add(new BasicAuthenticationInterceptor("test-user", "P4ssword"));
	}

	public <T> ResponseEntity<T> login(Class<T> responseType){
		return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
	}
	
	public <T> ResponseEntity<T> login2(ParameterizedTypeReference<T> responseType){
		return testRestTemplate.exchange(API_1_0_LOGIN, HttpMethod.POST, null, responseType);
	}
}
