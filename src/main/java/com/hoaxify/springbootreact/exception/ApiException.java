package com.hoaxify.springbootreact.exception;

import java.util.Date;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_NULL)
public class ApiException {

		private long timestamp = new Date().getTime();
		private int status;
		private String message;
		private String url;
		
		// Key: Field name failing with validation Value: Failed validation field values
		private Map<String, String> validationErrors; 
				
		public ApiException() {
			
		}
		
		public ApiException(int status, String message, String url) {
			super();
			this.status = status;
			this.message = message;
			this.url = url;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public Map<String, String> getValidationErrors() {
			return validationErrors;
		}

		public void setValidationErrors(Map<String, String> validationErrors) {
			this.validationErrors = validationErrors;
		}
		
		
		
}
