package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Product p where p.id = :id")
    java.util.Optional<Product> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    // Find all products in a category
    List<Product> findByCategoryId(Long categoryId);
    
    // Find products by name (for search)
    List<Product> findByNameContainingIgnoreCase(String name);
}
