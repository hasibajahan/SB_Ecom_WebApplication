package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Note: CategoryDTO is like the model(Category) but not exactly the model.
//it represents Category in the presentation layer. So that we can make changes easily.

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
	
	private Long categoryId;
	private String categoryName;
	
}
