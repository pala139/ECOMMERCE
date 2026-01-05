package com.ecommerce.exception;

/**
 * Custom exception for product-related errors.
 * Why this exists: Specific exceptions help in better error handling and debugging.
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }
    
    public ProductNotFoundException(String message) {
        super(message);
    }
}
