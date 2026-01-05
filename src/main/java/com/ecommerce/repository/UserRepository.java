package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * UserRepository handles database queries for User entity.
 * Why this interface exists: Spring Data JPA provides automatic implementation for common CRUD operations.
 * This reduces boilerplate code and provides type-safe database access.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Custom query: find user by email (for login)
    Optional<User> findByEmail(String email);
}
