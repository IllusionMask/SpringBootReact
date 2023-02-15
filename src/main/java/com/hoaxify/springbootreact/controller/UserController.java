package com.hoaxify.springbootreact.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hoaxify.springbootreact.exception.ApiException;
import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.model.UserUpdateVM;
import com.hoaxify.springbootreact.model.UserVM;
import com.hoaxify.springbootreact.service.UserService;
import com.hoaxify.springbootreact.shared.CurrentUser;
import com.hoaxify.springbootreact.shared.GenericResponse;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1.0/")
public class UserController {

	@Autowired
	UserService userService;
	
	@PostMapping("/users")
	public GenericResponse createUser(@Valid @RequestBody User user) { 
		 userService.saveUser(user);
		 return new GenericResponse("User Saved");
		 
		 //when validation error occur, it will throw MethodArgumentNotValidException
	}
	
	@GetMapping("/users")
	public Page<UserVM> getAllUser(@CurrentUser User loggedInUser, Pageable page){
		return userService.getAllUsers(loggedInUser, page).map((user) -> new UserVM(user)); // .map(UserVM:: new)
	}
	
	@GetMapping("/users/{username}")
	public UserVM getUserByUsername(@PathVariable String username){
		User user = userService.getByUsername(username);
		return new UserVM(user);
	}
	
	@PutMapping("/users/{id:[0-9]+}")
	@PreAuthorize("#id == principal.id")
	public UserVM updateUser(@PathVariable long id, @Valid @RequestBody(required = false) UserUpdateVM userUpdate){
		User user = userService.updateUser(id, userUpdate);
		return new UserVM(user);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiException handleValidationException(MethodArgumentNotValidException exception, HttpServletRequest request) {
		ApiException apiException = new ApiException(400, "Validation error", request.getServletPath());	
		
		BindingResult result = exception.getBindingResult();
		Map<String, String> validationErrors = new HashMap<>();
		
		for(FieldError fieldError: result.getFieldErrors()) {
			validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		
		apiException.setValidationErrors(validationErrors);
		
		return apiException;	
	}
}
