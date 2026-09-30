package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//to communicate between the service layer and the database layer.
//these classes are used to decouple the database layer from the service layer 

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
	private Long productId;
	private String productName;
	private String image;
	private String description;
	private Integer quantity;
	private double price;
	private double discount;
	private double specialPrice;
	
}
