package com.ecommerce.project.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;

@Component
public class AuthUtil {
	
	@Autowired
	UserRepository userRepository;
	
	//Getting the email of the logged in user
	public String loggedInEmail() {
	Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
	User user = userRepository.findByUserName(authentication.getName())
			.orElseThrow(() -> new UsernameNotFoundException("User not found with username: "+authentication.getName()));
	
	return user.getEmail();
	}
	
	//Getting the userId of the logged in user
	public Long loggedInUserId() {
		Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByUserName(authentication.getName())
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: "+authentication.getName()));
		
		return user.getUserId();
		}
	
	//Getting the User object
	public User loggedInUser() {
		Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
		User user = userRepository.findByUserName(authentication.getName())
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: "+authentication.getName()));
		
		return user;
		}
}
