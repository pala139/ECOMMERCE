package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@lombok.Getter
@lombok.Setter
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
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase;
    
    // Helper method to calculate total for this line item
    public BigDecimal getSubtotal() {
        return priceAtPurchase.multiply(new BigDecimal(quantity));
    }
}
