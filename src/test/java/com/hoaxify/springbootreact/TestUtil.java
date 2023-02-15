package com.hoaxify.springbootreact;

import com.hoaxify.springbootreact.model.User;

public class TestUtil {
	
	public static User createValidUser() {
		//Posting User object to server with HTTP client		
		User user = new User();
		user.setUsername("test-user");
		user.setDisplayName("test-display");
		user.setPassword("P4ssword");
		user.setImage("profile-image.png");
		return user;
	}
	
	public static User createValidUser(String username) {
		//Posting User object to server with HTTP client		
		User user = createValidUser();
		user.setUsername(username);
		return user;
	}
}
