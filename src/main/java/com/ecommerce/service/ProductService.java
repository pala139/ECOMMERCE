package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService handles product operations.
 * Why this class exists: Centralize product business logic.
 * Single Responsibility: Product CRUD operations and conversions.
 * 
 * Request flow:
 * 1. Controller receives request
 * 2. Service validates input and performs operations
 * 3. Converts Product entity to ProductDTO
 * 4. Returns DTO to controller
 * 
 * Interview talking point:
 * - We convert entities to DTOs because API contracts shouldn't expose database schema
 * - If we later change database fields, API clients don't break (if we update DTO mapping)
 * - Separation of concerns: database doesn't dictate API structure
 */
@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Get all products with pagination would be ideal, but keeping simple.
     * Convert entities to DTOs before returning to API.
     */
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get single product by ID.
     */
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToDTO(product);
    }
    
    /**
     * Create new product.
     * Validates category exists before creating.
     */
    public ProductDTO createProduct(CreateProductRequest request) {
        // Validate category exists
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        }
        
        // Create product entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        
        // Save and return
        product = productRepository.save(product);
        return convertToDTO(product);
    }
    
    /**
     * Search products by name.
     */
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Get products in a category.
     */
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update product stock when order is placed.
     * This is called by OrderService, so products and orders stay consistent.
     */
    public void decrementStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check stock availability
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }
        
        // Decrease stock
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
    
    /**
     * Helper: convert Product entity to ProductDTO.
     * Why separate conversion? If DTO structure changes, only this method updates.
     */
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        return dto;
    }
}
