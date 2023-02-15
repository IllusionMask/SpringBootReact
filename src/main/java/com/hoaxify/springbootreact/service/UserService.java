package com.hoaxify.springbootreact.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hoaxify.springbootreact.exception.DuplicateUsernameException;
import com.hoaxify.springbootreact.exception.NotFoundException;
import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.model.UserUpdateVM;
import com.hoaxify.springbootreact.service.impl.UserRepository;

@Service
public class UserService {

	UserRepository userRepository;
	
	PasswordEncoder passwordEncoder;
	
	FileService fileService;
	
	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileService fileService){
		super();
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileService = fileService;
	}
	
	public User saveUser(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	public Page<User> getAllUsers(User loggedInUser, Pageable pageable){
		if(loggedInUser != null) {
			return userRepository.findByUsernameNot(loggedInUser.getUsername(), pageable);
		}
		//Page start at 0 with 10 items
		return userRepository.findAll(pageable);
	}
	
	public User getByUsername(String username) {
		User userInDB = userRepository.findByUsername(username);
		if(userInDB == null) {
			throw new NotFoundException(username + " cannot be found.");
		}
		return userInDB;
	}

	public User updateUser(long id, UserUpdateVM userUpdate) {
		User userInDB = userRepository.getOne(id);
		userInDB.setDisplayName(userUpdate.getDisplayName());
		if(userUpdate.getImage() != null) {
			String savedImageName;
			try {
				savedImageName = fileService.saveProfileImage(userUpdate.getImage());
				fileService.deleteProfileImage(userInDB.getImage());
				userInDB.setImage(savedImageName);
			} catch (IOException e) {		
				e.printStackTrace();
			}			
		}	
		return userRepository.save(userInDB);
	}
		
}
