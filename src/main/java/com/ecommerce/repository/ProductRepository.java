package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Find all products in a category
    List<Product> findByCategoryId(Long categoryId);
    
    // Find products by name (for search)
    List<Product> findByNameContainingIgnoreCase(String name);
}
