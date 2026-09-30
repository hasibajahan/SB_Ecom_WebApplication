package com.ecommerce.project.service;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private FileService fileService;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private CartService cartService;
	
	@Value("${project.image}")
	private String path;

	@Override
	public ProductDTO addProduct(Long categoryId,ProductDTO productDTO) {
		
		Category category=categoryRepository.findById(categoryId)
				.orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));
		
		//Check if the product is already present or not
		boolean isProductNotPresent=true;
		
		List<Product> products=category.getProducts();
		for(Product value:products) {
			if(value.getProductName().equals(productDTO.getProductName())) {
				isProductNotPresent=false;
				break;
			}
		}
		if(isProductNotPresent) { 
		Product product=modelMapper.map(productDTO, Product.class);
		product.setImage("default.png");
		
		product.setCategory(category);
		
		double specialPrice=product.getPrice()-((product.getDiscount() * 0.01)*product.getPrice());//calculating special price
		product.setSpecialPrice(specialPrice);
		
		Product savedProduct=productRepository.save(product);
		
		return modelMapper.map(savedProduct, ProductDTO.class);
		}else {
			throw new APIException("Product already exists!!");
		}
		}

	//Get all products
	@Override
	public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
				?Sort.by(sortBy).ascending()
				:Sort.by(sortBy).descending();		
		
		Pageable pageDetails=PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts=productRepository.findAll(pageDetails);
		
		List<Product> products=pageProducts.getContent();
		List<ProductDTO> productDTOs=products.stream()
				.map(product->modelMapper.map(product, ProductDTO.class))
				.toList();
		
		//check if products exist or not(for validation)
		if(products.isEmpty()) {
			throw new APIException("No products exist.");
		}
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOs);
		
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		return productResponse;
	}

	//Get products by category
	@Override
	public ProductResponse searchByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		Category category=categoryRepository.findById(categoryId)
				.orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));
		
		
		Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
				?Sort.by(sortBy).ascending()
				:Sort.by(sortBy).descending();		
		
		Pageable pageDetails=PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);
		
		List<Product> products=pageProducts.getContent();
		List<ProductDTO> productDTOs=products.stream()
				.map(product->modelMapper.map(product, ProductDTO.class))
				.toList();
		
		//Validation
		if(products.size()==0) {
			throw new APIException("Category "+categoryId+" does not have any product");
		}
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOs);
		
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		
		return productResponse;
	}

	//Get products by keyword
	@Override
	public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
		
		Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
				?Sort.by(sortBy).ascending()
				:Sort.by(sortBy).descending();		
		
		Pageable pageDetails=PageRequest.of(pageNumber, pageSize, sortByAndOrder);
		Page<Product> pageProducts=productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageDetails);
		
		List<Product> products=pageProducts.getContent();
		
		List<ProductDTO> productDTOs=products.stream()
				.map(product->modelMapper.map(product, ProductDTO.class))
				.toList();
		
		//Validation
		if(products.size()==0) {
			throw new APIException("Products not found with keyword: "+keyword);
		}
		
		ProductResponse productResponse = new ProductResponse();
		productResponse.setContent(productDTOs);
		
		productResponse.setPageNumber(pageProducts.getNumber());
		productResponse.setPageSize(pageProducts.getSize());
		productResponse.setTotalElements(pageProducts.getTotalElements());
		productResponse.setTotalPages(pageProducts.getTotalPages());
		productResponse.setLastPage(pageProducts.isLast());
		
		return productResponse;
	}

	@Override
	public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
		//find the product
		Product productFromDb=productRepository.findById(productId)
				.orElseThrow(()->new ResourceNotFoundException("Product", "productId", productId));
		
		Product product=modelMapper.map(productDTO,Product.class);
		//update the product info with the one in the request Body
		productFromDb.setProductName(product.getProductName());
		productFromDb.setDescription(product.getDescription());
		productFromDb.setQuantity(product.getQuantity());
		productFromDb.setPrice(product.getPrice());
		productFromDb.setDiscount(product.getDiscount());
		double specialPrice=product.getPrice()-((product.getDiscount() * 0.01)*product.getPrice());
		productFromDb.setSpecialPrice(specialPrice);
		
		//save to database
		Product savedProduct=productRepository.save(productFromDb);
		
		List<Cart> carts=cartRepository.findCartByProductId(productId);
		
		List<CartDTO> cartDTOs=carts.stream().map(cart ->{
				CartDTO cartDTO=modelMapper.map(cart, CartDTO.class);
				
				List<ProductDTO> products=cart.getCartItems().stream()
						.map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());	
				
				cartDTO.setProducts(products);
				return cartDTO;
						}).collect(Collectors.toList());
		
		cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
		
		return modelMapper.map(savedProduct, ProductDTO.class);
	}

	@Override
	public ProductDTO deleteProduct(Long productId) {
		//first find the product
		Product productFromDb=productRepository.findById(productId)
				.orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
		
		//Delete product from the cart also
		List<Cart> carts=cartRepository.findCartByProductId(productId);
		carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
		
		//delete it
		productRepository.delete(productFromDb);
		return modelMapper.map(productFromDb,ProductDTO.class);
	}

	@Override
	public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException  {
		//1. Get the product from the DB
		Product productFromDb=productRepository.findById(productId)
				.orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));
		
		//2. Upload image to server
		//3. Get the file name of the uploaded image
		
//		String path="images/";//images in the root directory on the server(servers do have a location where images are uploaded)
		String filName=fileService.uploadImage(path,image);
		
		//4. Updating the new file name to the product
		productFromDb.setImage(filName);
		
		//5. Save the updated product
		Product updatedProduct=productRepository.save(productFromDb);
		
		//6. Return DTO after mapping product to DTO
		return modelMapper.map(updatedProduct, ProductDTO.class);
	}

	
}
