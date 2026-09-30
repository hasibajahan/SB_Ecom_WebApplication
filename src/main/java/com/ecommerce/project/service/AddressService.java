package com.ecommerce.project.service;

import java.util.List;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;


public interface AddressService {

	AddressDTO createAddress(AddressDTO addressDTO, User user);

	List<AddressDTO> getAllAddress();

	AddressDTO getAddressById(Long addressId);

	List<AddressDTO> getAddressByUser(User user);

	AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);

	String deleteAddress(Long addressId);

}
