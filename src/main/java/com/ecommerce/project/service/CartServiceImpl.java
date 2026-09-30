package com.ecommerce.project.service;


import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;

import jakarta.transaction.Transactional;

@Service
public class CartServiceImpl implements CartService{

	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private AuthUtil authUtil;
	
	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	CartItemRepository cartItemRepository;
	
	@Autowired
	ModelMapper modelMapper;
	
	@Override
	public CartDTO addProductToCart(Long productId, Integer quantity) {
		//Find existing cart or create one
		Cart cart=createCart();
		
		//Retrieve Product details
		Product product=productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId",productId));
		
		//Perform different validations
		CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(),productId);
		
		if(cartItem!=null) {
			throw new APIException("Product "+product.getProductName()+" already exists in the cart");
		}
		
		//checking the stock
		if(product.getQuantity() == 0) {
			throw new APIException(product.getProductName()+" is not available");
		}
		
		if(product.getQuantity()<quantity) {
			throw new APIException("Please, make an order of "+product.getProductName()
									+" less than or equal to the quantity "+product.getQuantity()+".");
		}
		
		//Create cart item
		CartItem newCartItem=new CartItem();
		newCartItem.setProduct(product);
		newCartItem.setCart(cart);
		newCartItem.setQuantity(quantity);
		newCartItem.setDiscount(product.getDiscount());
		newCartItem.setProductPrice(product.getSpecialPrice());
		
		//save cart item
		cartItemRepository.save(newCartItem);
		
		product.setQuantity(product.getQuantity());
		
		cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));
		
		cartRepository.save(cart);
		
		CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
		
		List<CartItem> cartItems=cart.getCartItems();
		
		Stream<ProductDTO> productStream=cartItems.stream().map(item ->{
								ProductDTO map=modelMapper.map(item.getProduct(),ProductDTO.class);
								map.setQuantity(item.getQuantity());
								return map;
								});
		cartDTO.setProducts(productStream.toList());									
		
		//return updated cart
		return cartDTO;
	}

	private Cart createCart() {
		Cart userCart=cartRepository.findCartByEmail(authUtil.loggedInEmail());
		if(userCart!=null) {
			return userCart;
		}
		Cart cart=new Cart();
		cart.setTotalPrice(0.00);
		cart.setUser(authUtil.loggedInUser());
		Cart newCart=cartRepository.save(cart);
		
		return newCart;
	}

	//Get all carts
	@Override
	public List<CartDTO> getAllCarts() {
		List<Cart> carts=cartRepository.findAll();
		
		if(carts.size()==0) {
			throw new APIException("No cart exists.");
		}
		
		List<CartDTO> cartDTOs=carts.stream().map(cart -> {
			CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
			
			List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                return productDTO;
            }).collect(Collectors.toList());
					
			cartDTO.setProducts(products);
			
			return cartDTO;
			
		}).collect(Collectors.toList());
	
		return cartDTOs;
	}

	//Get cart by id
	@Override
	public CartDTO getCart(String emailId, Long cartId) {
		Cart cart=cartRepository.findCartByEmailAndCartId(emailId, cartId);
		
		//Validation
		if(cart==null) {
			throw new ResourceNotFoundException("Cart","cartId",cartId);
		}
		
		CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);
		
		cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
		
		List<ProductDTO> products=cart.getCartItems().stream()
				.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
				.toList();
		cartDTO.setProducts(products);
		return cartDTO;
	}

	//To update the quantity of the items in the cart(add or delete)
	@Transactional
	@Override
	public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
		String emailId=authUtil.loggedInEmail();
		Cart userCart=cartRepository.findCartByEmail(emailId);
		Long cartId=userCart.getCartId();
		
		Cart cart=cartRepository.findById(cartId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
		
		Product product=productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
	
		//checking the stock
		if(product.getQuantity() == 0) {
			throw new APIException(product.getProductName()+" is out of stock.");
		}
		
		//Product is in stock but its' quantity is less than the quantity added in the cart
		if(product.getQuantity() < quantity) {
			throw new APIException("Please make an order of "+product.getProductName()
									+" less than or equal to the quantity " + product.getQuantity() + ".");
		}	
		CartItem cartItem=cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
		
		if(cartItem == null) {
			throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
		}
		
		// Calculate new quantity
        int newQuantity = cartItem.getQuantity() + quantity;

        // Validation to prevent negative quantities
        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if (newQuantity == 0){
            deleteProductFromCart(cartId, productId);
        } else {
        	//Update the product details
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }
		CartItem updatedItem = cartItemRepository.save(cartItem);
		
		if(updatedItem.getQuantity() == 0) {
			cartItemRepository.deleteById(updatedItem.getCartItemId());
		}
		
		CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
		
		List<CartItem> cartItems=cart.getCartItems();
		
		Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });
		
		cartDTO.setProducts(productStream.toList());
		
		return cartDTO;
	}
	
	//Rounds a double value to two decimal places to avoid floating-point precision issues in price calculations.
	private double roundToTwoDecimalPlaces(double value) {
	    return Math.round(value * 100.0) / 100.0;
	}
	
	//It will remove the product/item from the cart entirely
	@Transactional
	@Override
	public String deleteProductFromCart(Long cartId, Long productId) {
		Cart cart = cartRepository.findById(cartId)
	            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

	    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

	    if (cartItem == null) {
	        throw new ResourceNotFoundException("Product", "productId", productId);
	    }

	    // Calculate and round new total price
	    double updatedTotal = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
	    cart.setTotalPrice(roundToTwoDecimalPlaces(Math.max(updatedTotal, 0))); // ensure non-negative

	    cartItemRepository.deleteCartItemByCartIdAndProductId(cartId, productId);

	    return "Product " + cartItem.getProduct().getProductName() + " removed from the cart!!!";
	}

	@Override
	public void updateProductInCarts(Long cartId, Long productId) {
		 Cart cart = cartRepository.findById(cartId)
		            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

		    Product product = productRepository.findById(productId)
		            .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

		    CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

		    if (cartItem == null) {
		        throw new APIException("Product " + product.getProductName() + " not available in the cart.");
		    }

		    double itemTotal = cartItem.getProductPrice() * cartItem.getQuantity();
		    double updatedTotal = cart.getTotalPrice() - itemTotal;

		    cartItem.setProductPrice(product.getSpecialPrice());

		    updatedTotal += cartItem.getProductPrice() * cartItem.getQuantity();

		    cart.setTotalPrice(roundToTwoDecimalPlaces(Math.max(updatedTotal, 0))); // ensure non-negative

		    cartItemRepository.save(cartItem);
		}
	}

