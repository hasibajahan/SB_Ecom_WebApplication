package com.ecommerce.project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AddressController {
	
	@Autowired
	AuthUtil authUtil;
	
	@Autowired
	AddressService addressService;
	
	//Create Address
	@PostMapping("/addresses")
	public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){
		User user=authUtil.loggedInUser();
		AddressDTO savedAddress=addressService.createAddress(addressDTO,user);//The addresses will be associated with users. So, we need to pass the user as well.
		return new ResponseEntity<>(savedAddress,HttpStatus.CREATED);
	}
	
	//Get all addresses
	@GetMapping("/addresses")
	public ResponseEntity<List<AddressDTO>> getAllAddresses(){
		List<AddressDTO> addressList=addressService.getAllAddress();
		return new ResponseEntity<>(addressList,HttpStatus.OK);
	}
	
	//Get a specific address by address id
	@GetMapping("/addresses/{addressId}")
	public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){
		AddressDTO addressDTO=addressService.getAddressById(addressId);
		return new ResponseEntity<>(addressDTO,HttpStatus.OK);
	}
	
	//Get addresses of a specific user
	@GetMapping("/users/addresses")
	public ResponseEntity<List<AddressDTO>> getAddressByUser(){
		User user=authUtil.loggedInUser();
		List<AddressDTO> addressList=addressService.getAddressByUser(user);
		return new ResponseEntity<>(addressList,HttpStatus.OK);
	}
	
	//update address
	@PutMapping("/addresses/{addressId}")
	public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO addressDTO){
		AddressDTO updatedAddress=addressService.updateAddress(addressId,addressDTO);
		return new ResponseEntity<>(updatedAddress,HttpStatus.OK);
	}
	
	//Delete address
	@DeleteMapping("/addresses/{addressId}")
	public ResponseEntity<String> deleteAddress(@PathVariable Long addressId){
		String status=addressService.deleteAddress(addressId);
		return new ResponseEntity<>(status,HttpStatus.OK);
	}
}
