package com.ecommerce.project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.OrderItem;
import com.ecommerce.project.model.Payment;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.PaymentRepository;
import com.ecommerce.project.repositories.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderServiceImpl implements OrderService{

	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	AddressRepository addressRepository;
	
	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	OrderRepository orderRepository;
	
	@Autowired
	OrderItemRepository orderItemRepository;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CartService cartService;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Override
	@Transactional
	public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId,
			String pgStatus, String pgResponseMessage) {
		
		//validations
		Cart cart=cartRepository.findCartByEmail(emailId);
		if(cart==null) {
			throw new ResourceNotFoundException("Cart","email",emailId);
		}
		
		Address address=addressRepository.findById(addressId)
				.orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
		
		List<CartItem> cartItems=cart.getCartItems();
		if(cartItems.isEmpty()) {
			throw new APIException("Cart is empty!!");
		}
		
		//Create the order object
		Order order=new Order();
		order.setEmail(emailId);
		order.setOrderDate(LocalDate.now());
		order.setAddress(address);
		order.setOrderStatus("Order Confirmed !");
		order.setTotalAmount(cart.getTotalPrice());
		
		//Payment
		Payment payment=new Payment(paymentMethod, pgPaymentId, pgStatus, pgResponseMessage, pgName);
		payment.setOrder(order);
		payment=paymentRepository.save(payment);
		order.setPayment(payment);
		
		//save the order in the database
		Order savedOrder=orderRepository.save(order);
		
		//Now map the cartItems to orderItems
		List<OrderItem> orderItems=new ArrayList<>();//take a list of order items
		for(CartItem cartItem:cartItems) {
			OrderItem orderItem=new OrderItem();
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setDiscount(cartItem.getDiscount());
			orderItem.setOrderedProductPrice(cartItem.getProductPrice());
			orderItem.setOrder(savedOrder);
			orderItems.add(orderItem);
		}
		
		//save the orderitems in the database 
		orderItems = orderItemRepository.saveAll(orderItems);
		
		cart.getCartItems().forEach(item -> {
			int quantity=item.getQuantity();
			Product product=item.getProduct();
			
			//Reduce the stock quantity
			product.setQuantity(product.getQuantity() - quantity);
			
			//Save the product back to the database
			productRepository.save(product);
			
			//Also remove the items from the cart
			cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
		});
		
		OrderDTO orderDTO=modelMapper.map(savedOrder, OrderDTO.class);
		orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));
		
		orderDTO.setAddressId(addressId);
		
		return orderDTO;
	}

}
