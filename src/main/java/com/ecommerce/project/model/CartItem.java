package com.ecommerce.project.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="cart_items")
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long cartItemId;
	
	@ManyToOne   //many cart items can be added in one cart
	@JoinColumn(name="cart_id")
	private Cart cart;
	
	@ManyToOne   //we can add multiple products of the same type
	@JoinColumn(name="product_id")
	private Product product;
	
	private Integer quantity;
	private double discount;
	private double productPrice;
}
