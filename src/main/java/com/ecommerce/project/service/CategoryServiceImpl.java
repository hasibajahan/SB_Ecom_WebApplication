package com.ecommerce.project.service;


import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;

//we should write all the business logics in the service layer. Having the business logics in the controller layer is not a good practice. 

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ModelMapper modelMapper;
	
	// READ
	@Override
	public CategoryResponse getAllCategory(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {
		
		//For sorting the categories
		Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
				?Sort.by(sortBy).ascending()
				:Sort.by(sortBy).descending();
		
		//Pageable is an interface that represents the request for a specific page of data from the database.
		Pageable pageDetails=PageRequest.of(pageNumber, pageSize,sortByAndOrder);
		
		//Getting the page
		Page<Category> categoryPage=categoryRepository.findAll(pageDetails);
		
		List<Category> categories= categoryPage.getContent();
		if(categories.isEmpty()) {
			throw new APIException("No category created till now.");
		}
		
		List<CategoryDTO> categoryDTOs=categories.stream()
				.map(category -> modelMapper.map(category, CategoryDTO.class))
				.toList();
		CategoryResponse categoryResponse=new CategoryResponse();
		
		//getting the content
		categoryResponse.setContent(categoryDTOs);
		//getting all the pagination metadata along with the content
		categoryResponse.setPageNumber(categoryPage.getNumber());
		categoryResponse.setPageSize(categoryPage.getSize());
		categoryResponse.setTotalElements(categoryPage.getTotalElements());
		categoryResponse.setTotalPages(categoryPage.getTotalPages());
		categoryResponse.setLastPage(categoryPage.isLast());
		return categoryResponse;
	}

	// CREATE
	@Override
	public CategoryDTO createCategory(CategoryDTO categoryDTO) {
		Category category = modelMapper.map(categoryDTO, Category.class);
		Category categoryfromDB=categoryRepository.findByCategoryName(categoryDTO.getCategoryName());
		
		if(categoryfromDB!=null) {
			throw new APIException("Category with the name "+categoryDTO.getCategoryName()+" already exists!!!");
		}
		
		//HERE WE WERE SAVING THE CATEGORY DIRECTLY. SO THERE IS A POSSIBILITY OF HAVING 
		//DUPLICATE CATEGORY NAME. SO WE ADDED THE VALIDATION (ABOVE).
		Category savedCategory=categoryRepository.save(category);
		CategoryDTO savedCategoryDTO=modelMapper.map(savedCategory, CategoryDTO.class);
		return savedCategoryDTO;
	}

	// DELETE
	@Override
	public CategoryDTO deleteCategory(Long categoryId) {
		Category category=categoryRepository
				.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));
		
		categoryRepository.delete(category);
		return modelMapper.map(category, CategoryDTO.class);
	}

	// UPDATE
	@Override
	public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
		
		
		Category savedCategory = categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category","categoryId",categoryId));
		Category category=modelMapper.map(categoryDTO, Category.class);
		category.setCategoryId(categoryId);
         savedCategory = categoryRepository.save(category);
		return modelMapper.map(savedCategory, CategoryDTO.class);
	}
	
}
