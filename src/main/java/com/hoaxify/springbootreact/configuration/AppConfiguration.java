package com.hoaxify.springbootreact.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hoaxify")
public class AppConfiguration {

	String uploadPath;
	String profileImageFolder = "profile"; //Set default value even if not set in application.yml
	String attachmentFolder = "attachments";
	
	public AppConfiguration() {
		
	}

	public String getUploadPath() {
		return uploadPath;
	}

	public void setUploadPath(String uploadPath) {
		this.uploadPath = uploadPath;
	}
	
	public String getProfileImageFolder() {
		return profileImageFolder;
	}

	public void setProfileImageFolder(String profileImageFolder) {
		this.profileImageFolder = profileImageFolder;
	}

	public String getAttachmentFolder() {
		return attachmentFolder;
	}

	public void setAttachmentFolder(String attachmentFolder) {
		this.attachmentFolder = attachmentFolder;
	}

	public String getFullProfileImagePath() {
		return this.uploadPath + "/" + this.profileImageFolder;
	}
	
	public String getFullAttachmentsPath() {
		return this.uploadPath + "/" + this.attachmentFolder;
	}
}
