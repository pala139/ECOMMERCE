package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findForUpdate(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = "category")
    Page<Product> findAll(Pageable page);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable page);

    @EntityGraph(attributePaths = "category")
    Page<Product> findByCategoryId(Long categoryId, Pageable page);
}
