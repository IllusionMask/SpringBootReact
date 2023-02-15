package com.hoaxify.springbootreact.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.hoaxify.springbootreact.service.AuthUserService;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration{
	
		@Autowired
		AuthUserService authUserService;
	
	  @Bean
	  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		  
	    http.csrf().disable(); //Token generated for backend
	    
		http.headers().disable();
	
		http.httpBasic().authenticationEntryPoint(new BasicAuthenticationEntryPoint());
		//http.httpBasic();
		
		//Configure endpoint
	    http.authorizeRequests()
		.antMatchers(HttpMethod.POST, "/api/v1.0/login").authenticated()
		.antMatchers(HttpMethod.PUT, "/api/v1.0/users/{id:[0-9]+}").authenticated()
	    .and()
	    .authorizeRequests().anyRequest().permitAll();
	    
	    //REST server expected to be stateless.Mean Current request not affected by previous request.
	    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	    
	    return http.build();
	  }
	  
		@Bean
		public PasswordEncoder passwordEncoder() {
			return new BCryptPasswordEncoder();
		}	  
	  
		
		protected void configure(AuthenticationManagerBuilder auth)throws Exception{
			auth.userDetailsService(authUserService).passwordEncoder(passwordEncoder());
		}
	  
}
