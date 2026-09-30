package com.ecommerce.project.controller;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignUpRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	private JwtUtils jwtUtils;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	PasswordEncoder encoder;
	
	//Sign-in API
	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest){
		Authentication authentication;
		
		try {
			authentication=authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		}catch(AuthenticationException exception) {
			Map<String,Object> map=new HashMap<>();
			map.put("message","Bad credentials");
			map.put("status", false);
			return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
		}
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
	    UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
		ResponseCookie jwtCookie=jwtUtils.generateJwtCookie(userDetails);
		
		//getting the list of roles
		List<String> roles=userDetails.getAuthorities().stream()
				.map(item->item.getAuthority())
				.collect(Collectors.toList());
		
		UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);
	
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
				.body(response);
	}
	
	
	//Sign up API
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
		if(userRepository.existsByUserName(signUpRequest.getUserName())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));//So actually MessageResponse is a DTO that helps us convert the text based messages that we want to communicate to our end users in the JSON format.
		}
		
		if(userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}
		
		
		
		//Create new user's account
		User user=new User(signUpRequest.getUserName(),
							signUpRequest.getEmail(),
							encoder.encode(signUpRequest.getPassword()));
		
		Set<String> strRoles=signUpRequest.getRole();
		Set<Role> roles=new HashSet<>();
		
		if(strRoles==null) {
			Role userRole=roleRepository.findByRoleName(AppRole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		}else {
			strRoles.forEach(role -> {
				switch(role) {
				case "admin":
					Role adminRole=roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);
					break;
				case "seller":
					 Role modRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                     .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					 roles.add(modRole);
					 break;
				default:
					Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}
		
		user.setRoles(roles);
		userRepository.save(user);
		
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
	
	//Getting the current username
	@GetMapping("/username")
	public String currentUserName(Authentication authentication) {
		if(authentication != null) {
			return authentication.getName();
		}else {
			return "NULL";
		}
	}
	//Getting the current userdetails
	@GetMapping("/user")
	public ResponseEntity<?> getUserDetails(Authentication authentication) {
		UserDetailsImpl userDetails=(UserDetailsImpl) authentication.getPrincipal();
		
		//getting the list of roles
		List<String> roles=userDetails.getAuthorities().stream()
					.map(item -> item.getAuthority())
					.collect(Collectors.toList());
				
		UserInfoResponse response=new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles);
		
		return ResponseEntity.ok().body(response);
}
	
	//sign out endpoint
	@PostMapping("/signout")
	public ResponseEntity<?> signoutUser(){
		ResponseCookie cookie=jwtUtils.getCleanJwtCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(new MessageResponse("You've been signed out!"));
	}
}
