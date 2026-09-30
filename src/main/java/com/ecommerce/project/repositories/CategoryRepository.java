package com.ecommerce.project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.project.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> 
//the two parameters of the jpa repository are type of the entity and the type of the primary key of the entity.
{
	//Note: jpa will analyze this method declaration and understand what we want and it will
	//automatically generate the implementation on the go.
	
	//How JPA is going to figure out the implementation:-
	// It will analyze the method name first. So method name has to follow some certain convention.
	// 1) "findBy" prefix indicates that this is a query method.
	// 2) "CategoryName" indicates a field name defined in a certain entity.
	
	// So JPA simply parse the method name to understand the intended query and it will create a proxy instance in the hindsight.
	Category findByCategoryName(String categoryName);
	
}
