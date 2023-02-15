package com.hoaxify.springbootreact;

import java.util.stream.IntStream;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.hoaxify.springbootreact.model.User;
import com.hoaxify.springbootreact.service.UserService;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class SpringbootReactHoaxifyProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootReactHoaxifyProjectApplication.class, args);
	}

	@Bean
	@Profile("dev")
	CommandLineRunner run(UserService userService) {
		
		return new CommandLineRunner() {
			
			@Override
			public void run(String... args) throws Exception {
				IntStream.rangeClosed(1, 15).mapToObj(i->{
					User user = new User();
					user.setUsername("user" + i);
					user.setDisplayName("user" + i);
					user.setPassword("P4ssword");
					return user;
				})
				.forEach(user -> userService.saveUser(user));
				
			}
		};
		
	}
}
