package com.ecommerce.service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderItemDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * OrderService handles order operations.
 * Why this class exists: Complex logic for order creation and management.
 * Single Responsibility: Handle order CRUD and business logic.
 * 
 * Request flow for creating order:
 * 1. Client sends order request with items and shipping address
 * 2. Service fetches authenticated user
 * 3. For each item:
 *    a. Fetch product from database
 *    b. Check stock availability
 *    c. Create OrderItem with current product price (frozen in time)
 *    d. Decrement product stock
 * 4. Create Order with all items
 * 5. Calculate total amount (sum of all item subtotals)
 * 6. Return order to client
 * 
 * Interview talking point:
 * - Why store priceAtPurchase in OrderItem? Product price changes shouldn't affect past orders
 * - Why generate orderNumber? Users need a human-readable reference (timestamps are ugly)
 * - Why check stock before order? Prevent overselling
 * - Why transaction? If one item fails, whole order fails - keep data consistent
 *   (In production, add @Transactional annotation)
 */
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Create a new order for authenticated user.
     */
    public OrderDTO createOrder(String userEmail, CreateOrderRequest request) {
        // Find user by email (set by JWT filter)
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create order entity
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZipCode(request.getShippingZipCode());
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Process each item in the order
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Check stock before adding to order
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Create order item with frozen price
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());
            
            order.getItems().add(orderItem);
            
            // Add to total
            totalAmount = totalAmount.add(orderItem.getSubtotal());
            
            // Decrement stock
            productService.decrementStock(itemRequest.getProductId(), itemRequest.getQuantity());
        }
        
        // Set total and save order
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);
        
        return convertToDTO(order);
    }
    
    /**
     * Get order by ID (for order details).
     */
    public OrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        return convertToDTO(order);
    }
    
    /**
     * Get all orders for a user.
     */
    public List<OrderDTO> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return orderRepository.findByUserId(user.getId())
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * Update order status (PENDING -> CONFIRMED -> SHIPPED -> DELIVERED).
     */
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(newStatus);
        order = orderRepository.save(order);
        
        return convertToDTO(order);
    }
    
    /**
     * Generate unique order number (ORD-UUID).
     * Why? Humans need readable references, not database IDs.
     */
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Helper: convert Order entity to OrderDTO with nested items.
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingCity(order.getShippingCity());
        dto.setShippingState(order.getShippingState());
        dto.setShippingZipCode(order.getShippingZipCode());
        
        // Convert items
        dto.setItems(order.getItems().stream()
            .map(this::convertItemToDTO)
            .collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * Helper: convert OrderItem entity to OrderItemDTO.
     */
    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtPurchase(item.getPriceAtPurchase());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}
