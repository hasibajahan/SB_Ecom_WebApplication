package com.ecommerce.project.controller;


import com.ecommerce.project.configuration.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CategoryController {
	
	@Autowired
	private CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		super();
		this.categoryService = categoryService;
	}

	//to know the page number and no of objects in each page 
	@GetMapping("/echo")
	public ResponseEntity<String> echoMessage(@RequestParam(name="message", required = false) String message){

		//	public ResponseEntity<String> echoMessage(@RequestParam(name="message", defaultValue = "Hi") String message){
		return new ResponseEntity<>("Echoed message: "+message,HttpStatus.OK);
	}
	
	//using ResponseEntity in all end points
	@GetMapping("/public/categories")
	public ResponseEntity<CategoryResponse> getAllCategories(
			@RequestParam(name="pageNumber",defaultValue=AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue=AppConstants.PAGE_SIZE,required=false) Integer pageSize,
			@RequestParam(name="sortBy",defaultValue=AppConstants.SORT_CATEGORIES_BY,required=false) String sortBy,
			@RequestParam(name="sortOrder",defaultValue=AppConstants.SORT_DIRECTION,required=false) String sortOrder) {
		 
		CategoryResponse categories=categoryService.getAllCategory(pageNumber,pageSize,sortBy,sortOrder);
		 return new ResponseEntity<>(categories,HttpStatus.OK);
	}

	@PostMapping("/public/categories")
	public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
		CategoryDTO savedCategoryDTO=categoryService.createCategory(categoryDTO);
		return new ResponseEntity<>(savedCategoryDTO,HttpStatus.CREATED);
	}
	
	//handling the not found exception using try-catch.
	@DeleteMapping("/admin/categories/{categoryId}")
	public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {
		
		CategoryDTO deletedCategory=categoryService.deleteCategory(categoryId);
		return new ResponseEntity<>(deletedCategory, HttpStatus.OK);
		}
	
	
	//update category(similar to the create category)
	@PutMapping("/public/categories/{categoryId}")
	public ResponseEntity<CategoryDTO> updateCategory(@Valid @RequestBody CategoryDTO categoryDTO,
												@PathVariable Long categoryId) {
	
			CategoryDTO savedCategoryDTO=categoryService.updateCategory(categoryDTO,categoryId);
			return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);
	}
	
	
}
