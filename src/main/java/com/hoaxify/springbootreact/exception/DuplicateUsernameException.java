package com.hoaxify.springbootreact.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateUsernameException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	
	//Whenever records not exist/found in DB, then it will throw the exception
	//Return Not_Found status to client
	public DuplicateUsernameException(String message) {
		super(message);
	}

}
