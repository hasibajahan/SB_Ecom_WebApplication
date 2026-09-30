package com.ecommerce.project.security.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import com.ecommerce.project.security.services.UserDetailsImpl;

import java.security.Key;
import java.util.Date;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {
	  private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	  @Value("${spring.app.jwtSecret}")
	  private String jwtSecret;

	  @Value("${spring.app.jwtExpirationMs}")
	  private int jwtExpirationMs;
	  
	  @Value("${spring.ecom.app.jwtCookieName}")
	  private String jwtCookie;
	  
	  //Get the jwt token from cookie
	  public String getJwtFromCookies(HttpServletRequest request) {
		  Cookie cookie=WebUtils.getCookie(request, jwtCookie);
		  if(cookie!= null) {
			  return cookie.getValue();
		  }else {
			  return null;
		  }
	  }
	  
	  //Generate cookie from jwt token
	  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
		  String jwt=generateTokenFromUsername(userPrincipal.getUsername());
		  ResponseCookie cookie=ResponseCookie.from(jwtCookie,jwt)
				  .path("/api")
				  .maxAge(24*60*60)
				  .httpOnly(false)
				  .build();
		  return cookie;
	  }
	  
	  //Getting clean cookie(needed for sign out purpose)
	  public ResponseCookie getCleanJwtCookie() {
		  ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
				  .path("/api")
				  .build();
		  return cookie;
	  }
	  
	  //Getting the token from the http header
	  public String getJwtFromHeader(HttpServletRequest request) {
		  String bearerToken=request.getHeader("Authorization");
		  logger.debug("Authorization Header: {}",bearerToken);
		  if(bearerToken!=null && bearerToken.startsWith("Bearer ")) {
			  return bearerToken.substring(7);//Removing the bearer prefix
		  }
		  return null;
		  }
	  
	  //Generate token from username
	  public String generateTokenFromUsername(String username) {
		   		return Jwts.builder()
		   				.subject(username)
		   				.issuedAt(new Date())
		   				.expiration(new Date((new Date()).getTime()+jwtExpirationMs))
		   				.signWith(key())
		   				.compact();
		  }
	  
	  //Getting the username from the jwt token
	  public String getUserNameFromJwtToken(String token) {
		    return Jwts.parser()
		    		.verifyWith((SecretKey) key())
		    		.build()
		    		.parseSignedClaims(token)
		    		.getPayload()
		    		.getSubject();
		    		
	  }
	  
	  private Key key() {
	    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
	  }

	 
	  //for validating the token
	  public boolean validateJwtToken(String authToken) {
	    try {
	      Jwts.parser().verifyWith((SecretKey) key()).build().parseSignedClaims(authToken); 
	      return true;
	    } catch (MalformedJwtException e) {
	      logger.error("Invalid JWT token: {}", e.getMessage());
	    } catch (ExpiredJwtException e) {
	      logger.error("JWT token is expired: {}", e.getMessage());
	    } catch (UnsupportedJwtException e) {
	      logger.error("JWT token is unsupported: {}", e.getMessage());
	    } catch (IllegalArgumentException e) {
	      logger.error("JWT claims string is empty: {}", e.getMessage());
	    }

	    return false;
	  }
	}
