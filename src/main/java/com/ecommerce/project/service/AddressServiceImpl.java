package com.ecommerce.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;

@Service
public class AddressServiceImpl implements AddressService{
	
	@Autowired
	private AddressRepository addressRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	 @Autowired
	 UserRepository userRepository;


	@Override
	public AddressDTO createAddress(AddressDTO addressDTO, User user) {
		
		//Convert the AddressDTO to the Address entity
		Address address=modelMapper.map(addressDTO, Address.class);
		
		//Get the addresses from the user
		List<Address> addressList=user.getAddresses();
		
		//Add the newly created address to the address list of the logged in user
		addressList.add(address);
		
		//update the user object with the new address list 
		user.setAddresses(addressList);
		
		//add user to the address also(as they consist a ManyToMany bidirectional relationship)
		address.setUser(user);
		
		//Save the new address into the database
		Address savedAddress = addressRepository.save(address);
		
		//Convert the saved entity back to a DTO
		AddressDTO savedAddressDTO=modelMapper.map(savedAddress, AddressDTO.class);
		
		//Return the saved AddressDTO as a response
		return savedAddressDTO;
	}

	@Override
	public List<AddressDTO> getAllAddress() {
		List<Address> addresses=addressRepository.findAll();
		List<AddressDTO> addressDTOs=addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class))
		.toList();
	
		return addressDTOs; 
	}

	@Override
	public AddressDTO getAddressById(Long addressId) {
		Address address=addressRepository.findById(addressId)
	                           .orElseThrow(() -> new ResourceNotFoundException("Address","addressId",addressId));	
		AddressDTO addressDTO=modelMapper.map(address, AddressDTO.class);
		return addressDTO;
	}

	@Override
	public List<AddressDTO> getAddressByUser(User user) {
		List<Address> addresses=user.getAddresses();
		return addresses.stream().
					map(address -> modelMapper.map(address, AddressDTO.class))
					.toList();
	}

	@Override
	public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
		//Fetch the address from the database
		Address addressFromDb=addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address","addressId",addressId));
		
		//Update all the fields of the particular address
		addressFromDb.setBuildingName(addressDTO.getBuildingName());
		addressFromDb.setCity(addressDTO.getCity());
		addressFromDb.setCountry(addressDTO.getCountry());
		addressFromDb.setPincode(addressDTO.getPincode());
		addressFromDb.setState(addressDTO.getState());
		addressFromDb.setStreet(addressDTO.getStreet());
		
		//save the updated address in the database
		Address updatedAddress=addressRepository.save(addressFromDb);
		
		//Update the associated user also
		User user=addressFromDb.getUser();
		user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
		user.getAddresses().add(updatedAddress);
		userRepository.save(user);
		
		return modelMapper.map(updatedAddress, AddressDTO.class);
	}

	//delete address
	@Override
	public String deleteAddress(Long addressId) {
		//find the address by id by using addressRepository
		Address addressFromDb = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
		
		//Remove the address from the associated user as well(in the database)
		User user=addressFromDb.getUser();
		user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
		userRepository.save(user);
		
		//delete it
		addressRepository.delete(addressFromDb);
		
		return "Address deleted successfully with addressId: " + addressId;
	}
}
