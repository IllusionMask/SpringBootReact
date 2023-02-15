package com.hoaxify.springbootreact.controller;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.hoaxify.springbootreact.exception.ApiException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class ExceptionHandler implements ErrorController {
	
	@Autowired
	private ErrorAttributes errorAttributes;
	
	@RequestMapping("/error")
	ApiException handleError(WebRequest webRequest) {
		Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.of(Include.MESSAGE));
		
		String message = (String) attributes.get("message");
		String url = (String) attributes.get("path");
		int status = (Integer) attributes.get("status");
		return new ApiException(status, message, url);
	}

	
	public String getErrorPath() {
		return "/error";
	}
}
