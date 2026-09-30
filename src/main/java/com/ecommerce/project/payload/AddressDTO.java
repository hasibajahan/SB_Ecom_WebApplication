package com.ecommerce.project.payload;

//Data Transfer Object (DTO) is used to transfer data between different layers of an application, 
//typically between the controller and service layers in a Spring Boot application.

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
	
	private Long addressId;

	private String street;

	private String buildingName;

	private String city;
	
	private String state;
	
	private String country;

	private String pincode;
}
