package com.hoaxify.springbootreact.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.service.impl.UserRepository;

@Service
public class AuthUserService implements UserDetailsService {
	//Tell Spring security to check incoming request's user information in database
	
	@Autowired
	UserRepository userRepository;
	
	//Call whenever request is receive with user credential
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			throw new UsernameNotFoundException("User not found");
		}
		return user;
	}
	
}
