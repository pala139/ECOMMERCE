package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity represents a customer's purchase.
 * Why this class exists: Track customer orders, status, and payment information.
 * Single Responsibility: Store order header information and relationships to items.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String orderNumber; // Unique order identifier for customers
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    // Status: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    @Column(nullable = false)
    private String status;
    
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
        status = "PENDING"; // Default status
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
