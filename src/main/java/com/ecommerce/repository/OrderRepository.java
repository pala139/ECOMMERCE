package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserEmail(Long id, String email);
    Optional<Order> findByUserIdAndIdempotencyKey(Long userId, String key);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select o from Order o where o.id = :id")
    Optional<Order> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    Page<Order> findByUserId(Long userId, Pageable page);
}
