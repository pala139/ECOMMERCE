package com.ecommerce.controller;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ProductController exposes product operations via REST API.
 * Why this class exists: HTTP endpoint handler for product CRUD.
 * Single Responsibility: Handle HTTP requests/responses for products.
 * 
 * Request flow:
 * 1. GET /products - List all products
 * 2. GET /products/{id} - Get product details
 * 3. GET /products/search?keyword=... - Search products
 * 4. GET /products/category/{categoryId} - Products in category
 * 5. POST /products - Create product (admin only, but we keep it simple)
 * 
 * Interview talking point:
 * - Consistent REST naming: plural nouns for collections
 * - HTTP methods: GET (read), POST (create), PUT (update), DELETE (delete)
 * - Controller delegates all logic to service
 * - Response entities allow control of HTTP status codes
 */
@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    /**
     * GET /products - List all products.
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /products/{id} - Get product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    /**
     * GET /products/search?keyword=xxx - Search products by name.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> products = productService.searchProducts(keyword);
        return ResponseEntity.ok(products);
    }
    
    /**
     * GET /products/category/{categoryId} - Get products in category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(products);
    }
    
    /**
     * POST /products - Create new product.
     * Request: { name, description, price, stockQuantity, categoryId }
     * Response: ProductDTO
     * @Valid triggers validation of CreateProductRequest
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}
