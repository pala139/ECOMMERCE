package com.ecommerce.controller;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * OrderController exposes order operations via REST API.
 * Why this class exists: HTTP endpoint handler for order CRUD.
 * Single Responsibility: Handle HTTP requests/responses for orders.
 * 
 * Request flow for creating an order:
 * 1. Client sends authenticated request (JWT in Authorization header)
 * 2. JwtAuthenticationFilter validates token, sets email in SecurityContext
 * 3. Controller receives Authentication object with user's email
 * 4. Pass email to OrderService to fetch correct user
 * 5. Service creates order and returns OrderDTO
 * 6. Controller returns 201 CREATED
 * 
 * Interview talking point:
 * - Authentication object comes from SecurityContext set by filter
 * - We use getPrincipal() to get the email (set by JwtAuthenticationFilter)
 * - Every order is tied to authenticated user - no accidental data leaks
 * - REST endpoints follow standard patterns
 */
@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * POST /orders - Create new order.
     * Request: { items: [{productId, quantity}, ...], shippingAddress, shippingCity, ... }
     * Response: OrderDTO
     * 
     * Authentication parameter is injected by Spring Security.
     * It contains the authenticated user's email (from JWT token).
     * @Valid triggers validation of CreateOrderRequest
     */
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        
        // Get authenticated user's email from SecurityContext
        String userEmail = authentication.getPrincipal().toString();
        
        // Create order
        OrderDTO order = orderService.createOrder(userEmail, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
    
    /**
     * GET /orders - Get all orders for authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getUserOrders(Authentication authentication) {
        String userEmail = authentication.getPrincipal().toString();
        List<OrderDTO> orders = orderService.getUserOrders(userEmail);
        return ResponseEntity.ok(orders);
    }
    
    /**
     * GET /orders/{id} - Get order details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
    /**
     * PUT /orders/{id}/status - Update order status.
     * Request body: { "status": "CONFIRMED" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        
        OrderDTO order = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }
    
    /**
     * Helper class for status update request.
     */
    public static class StatusUpdateRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
