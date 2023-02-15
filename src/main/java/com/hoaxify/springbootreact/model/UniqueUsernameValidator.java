package com.hoaxify.springbootreact.model;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.hoaxify.springbootreact.service.impl.UserRepository;

public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {

	@Autowired
	UserRepository userRepository;
	
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		User userInDB = userRepository.findByUsername(value);
		if(userInDB == null) {
			return true;
		}
		return false;
	}

}
