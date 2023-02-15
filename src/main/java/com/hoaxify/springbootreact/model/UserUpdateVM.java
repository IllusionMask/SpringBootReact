package com.hoaxify.springbootreact.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.hoaxify.springbootreact.shared.ProfileImage;

public class UserUpdateVM {

	//Only fields that can be changed during update
	@NotNull
	@Size(min=4, max=255)
	private String displayName;

	@ProfileImage
	private String image;
	
	public UserUpdateVM() {
		
	}	
	
	public UserUpdateVM(String displayName, String image) {
		super();
		this.displayName = displayName;
		this.image = image;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	
}
