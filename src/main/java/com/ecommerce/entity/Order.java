package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "idempotency_key"}))
@lombok.Getter
@lombok.Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true)
    private String orderNumber; // Unique order identifier for customers
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 128)
    private String idempotencyKey;
    @Column(length = 64)
    private String requestHash;
    
    // Status: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    @Column(nullable = false)
    private String status = "PENDING";
    
    // Delivery address (may differ from user's default address)
    private String shippingAddress;
    
    private String shippingCity;
    
    private String shippingState;
    
    private String shippingZipCode;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Cascade: delete OrderItems when Order is deleted
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
