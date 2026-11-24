package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.email = :email")
    java.util.Optional<User> findForCheckout(@org.springframework.data.repository.query.Param("email") String email);

    // Custom query: find user by email (for login)
    Optional<User> findByEmail(String email);
}
