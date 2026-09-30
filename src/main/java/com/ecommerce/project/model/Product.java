package com.ecommerce.project.model;



import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


//This class represents a single product

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name="products")
@ToString
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long productId;
	
	@NotBlank
	@Size(min=3,message = "Product name must contain atleast 3 characters.")
	private String productName;
	private String image;
	@NotBlank
	@Size(min=6,message = "Product description must contain atleast 6 characters.")
	private String description;
	private Integer quantity;
	private double price;//example:Rs 1000
	private double discount;//25%
	private double specialPrice;//price-(discount*(1/100)*price) ---> (1000-(1000*0.25))
	
	@ManyToOne
	@JoinColumn(name="category_id")
	private Category category; 
	
	@ManyToOne
	@JoinColumn(name="seller_id")
	private User user;
	
	@OneToMany(mappedBy="product",
			cascade = {CascadeType.PERSIST,CascadeType.MERGE},
			fetch=FetchType.EAGER)
	private List<CartItem> products=new ArrayList<>();
}
