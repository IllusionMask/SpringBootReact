package com.hoaxify.springbootreact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import com.hoaxify.springbootreact.configuration.AppConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class StaticResourceTest {

	@Autowired
	AppConfiguration appConfiguration;
	
	@Autowired
	MockMvc mockMvc; // Client
	
	@Test
	public void checkStaticFolder_whenAppInitialized_uploadFileFolderMustExist() {
		File uploadFolder = new File(appConfiguration.getUploadPath());
		boolean uploadFolderExist = uploadFolder.exists() && uploadFolder.isDirectory();
		
		assertThat(uploadFolderExist).isTrue();		
	}
	
	@Test
	public void checkStaticFolder_whenAppInitialized_profileImageSubFolderMustExist() {
		String profileImagePath = appConfiguration.getFullProfileImagePath();
		
		File profileImageFolder = new File(profileImagePath);
		boolean profileImageFolderExist = profileImageFolder.exists() && profileImageFolder.isDirectory();
		
		assertThat(profileImageFolderExist).isTrue();		
	}
	
	@Test
	public void checkStaticFolder_whenAppInitialized_attachmentSubFolderMustExist() {
		String attachmentFolderPath = appConfiguration.getFullAttachmentsPath();
		
		File attachmentFolder = new File(attachmentFolderPath);
		boolean attachmentFolderExist = attachmentFolder.exists() && attachmentFolder.isDirectory();
		
		assertThat(attachmentFolderExist).isTrue();		
	}
	
	@Test
	public void getStaticFile_whenImageExistInProfileUploadFolder_receiveOk() throws Exception{
		//Image
		String fileName = "profile-picture.png";
		//Image file
		File source = new ClassPathResource("profile.png").getFile();
		
		//Copy to upload folder/target file
		File target = new File(appConfiguration.getFullProfileImagePath()+ "/" + fileName);
		FileUtils.copyFile(source, target);
		
		//Make HTTP request to get file
		mockMvc.perform(get("/images/" + appConfiguration.getProfileImageFolder() + "/" + fileName)).andExpect(status().isOk());
	}
	
	@Test
	public void getStaticFile_whenImageExistInAttachmentFolder_receiveOk() throws Exception{
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
			
		File target = new File(appConfiguration.getFullAttachmentsPath()+ "/" + fileName);
		FileUtils.copyFile(source, target);
	
		mockMvc.perform(get("/images/" + appConfiguration.getAttachmentFolder() + "/" + fileName)).andExpect(status().isOk());
	}
	
	@Test
	public void getStaticFile_whenImageDoesNotExist_receiveNotFound() throws Exception{
		mockMvc.perform(get("/images/" + appConfiguration.getAttachmentFolder() + "/thereisnosuchimage.png")).andExpect(status().isNotFound());
	}
	
	@Test
	public void getStaticFile_whenImageExistInAttachmentFolder_receiveOkWithCacheHeaders() throws Exception{
		String fileName = "profile-picture.png";
		File source = new ClassPathResource("profile.png").getFile();
			
		File target = new File(appConfiguration.getFullAttachmentsPath()+ "/" + fileName);
		FileUtils.copyFile(source, target);
	
		MvcResult result = mockMvc.perform(get("/images/" + appConfiguration.getAttachmentFolder() + "/" + fileName)).andReturn();
		String cacheControl = result.getResponse().getHeaderValue("Cache-Control").toString();
		
		//Cache the file for a year
		assertThat(cacheControl).containsIgnoringCase("max-age=31536000");
	}
	
	
	@After
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(appConfiguration.getFullProfileImagePath()));
		FileUtils.cleanDirectory(new File(appConfiguration.getFullAttachmentsPath()));
	}
}
