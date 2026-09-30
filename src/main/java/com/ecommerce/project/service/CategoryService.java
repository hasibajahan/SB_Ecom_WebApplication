package com.ecommerce.project.service;




import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;

//using interface to achieve loose coupling and modularity in the code base
public interface CategoryService {
	
	CategoryResponse getAllCategory(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder);
    CategoryDTO createCategory(CategoryDTO categoryDTO);
	CategoryDTO deleteCategory(Long categoryId);
	CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
