package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * OrderItem entity represents individual products in an order.
 * Why this class exists: Store line items for orders - what products were ordered and in what quantity.
 * Single Responsibility: Track product quantities and prices for each order.
 * This is separate from Product because price may change after order is placed.
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    // Store quantity ordered - independent of stock changes
    @Column(nullable = false)
    private Integer quantity;
    
    // Store price at time of order - insulates from future price changes
    @Column(nullable = false)
    private BigDecimal priceAtPurchase;
    
    // Helper method to calculate total for this line item
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(new BigDecimal(quantity));
    }
}
