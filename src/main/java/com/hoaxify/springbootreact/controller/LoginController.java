package com.hoaxify.springbootreact.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoaxify.springbootreact.exception.ApiException;
import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.model.UserVM;
import com.hoaxify.springbootreact.shared.CurrentUser;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1.0/")
public class LoginController {

	@PostMapping("/login")
	public UserVM handleLogin(@CurrentUser User loggedInUser) {
		//Get Logged In user (Without @CurrentUser) Get from security context
		//User loggedInUser = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//return Collections.singletonMap("id", loggedInUser.getId());  //(key, value)
		
//		Since return User, remove map
//		Map<String, Object> userMap = new HashMap<>();
//		userMap.put("id", loggedInUser.getId());
//		userMap.put("image", loggedInUser.getImage());
		
		return new UserVM(loggedInUser);
	}
	
	//Cannot work as SPring handle internally before reaching this exceptioh handler,
	//therefore create new ExceptionHandler.java class
	@ExceptionHandler({AccessDeniedException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ApiException handleAccessDeniedException() {
		return new ApiException(401, "Access Error", "/api/v1.0/login");
	}
}
