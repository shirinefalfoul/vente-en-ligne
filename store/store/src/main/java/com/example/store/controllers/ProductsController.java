package com.example.store.controllers;

import java.io.InputStream;

import java.nio.file.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.store.services.ProductsRepository;

import jakarta.validation.Valid;

import com.example.store.models.ProductDto;
import com.example.store.models.product;

@Controller 
@RequestMapping ("/products")
public class ProductsController {

	@Autowired
	private ProductsRepository repo ;
	
	//read the products from the database 
	@GetMapping ({"","/"})
	public String showProductList (Model model) {
		List<product> products = repo.findAll();
		model.addAttribute ("products", products) ;
		return "products/index";
	}
	
	@GetMapping ("/create")
	public String showCreatePage (Model model) {
		ProductDto productDto = new ProductDto();
		model.addAttribute ("productDto", productDto) ;
		return "products/CreateProduct";
	}
	
	
	//in the @valid we have to validate the data that has been entered in the form this is from the validation dependency 
	//and the BindingResult is to get the result of the validation and store it in the object result.
	@PostMapping("/create")
	public String createProduct(
	        @Valid @ModelAttribute ProductDto productDto, 
	        BindingResult result) {
	    
	    // Custom validation for the image file
	    if (productDto.getImageFile().isEmpty()) {
	        result.addError(new FieldError("productDto", "imageFile", "The image file is required!"));
	    }

	    // Check if there are any validation errors
	    if (result.hasErrors()) {
	        return "products/CreateProduct"; // Return to the form view if there are validation errors
	    }

	    //save image file on the server : 
	    MultipartFile image = productDto.getImageFile();
	    Date createdAt = new Date(); 
	    String storageFileName = createdAt.getTime()+ "_" + image.getOriginalFilename();
	    try {
	    	String uploadDir = "public/images"; 
	    	Path uploadPath = Paths.get(uploadDir); 
	    	
	    	if (!Files.exists(uploadPath)) {
	    		Files.createDirectories(uploadPath);
	    	}
	    	try (InputStream inputStream = image.getInputStream()){
	    		Files.copy(inputStream, Paths.get(uploadDir + storageFileName),StandardCopyOption.REPLACE_EXISTING);
	    	}
	    }catch (Exception e) {
	    	System.out.println("Exception:" + e.getMessage());
	    }
	    
	    product product = new product (); 
	    product.setName(productDto.getName());
	    product.setBrand(productDto.getBrand());
	    product.setCategory(productDto.getCategory());
	    product.setPrice(productDto.getPrice());
	    product.setDescription(productDto.getDescription());
	    product.setCreatedAt(createdAt);
	    product.setImageFileName(storageFileName);
	    
	    repo.save(product); //save the product in the database 
	    
	    
	    return "redirect:/products"; // Redirect only if the product is successfully saved
	    
	 
	}

	@GetMapping ("/edit")
	public String showEditPage (Model model , @RequestParam int id) {
		
		try {
			product product = repo.findById(id).get(); //the return type is optional so this means the product can be present in the DB or not so we use .get() to get the actual product if it's existing 
			model.addAttribute("product",product);
			
			  ProductDto productDto = new ProductDto(); 
			  productDto.setName(product.getName());
			  productDto.setBrand(product.getBrand());
			  productDto.setCategory(product.getCategory());
			  productDto.setPrice(product.getPrice());
			  productDto.setDescription(product.getDescription());
			  
			  model.addAttribute("productDto",productDto);
				
			    
			
		}catch (Exception e) {
	    	System.out.println("Exception:" + e.getMessage());
	    }
		
		return "products/EditProduct";
	}
	
	

}
