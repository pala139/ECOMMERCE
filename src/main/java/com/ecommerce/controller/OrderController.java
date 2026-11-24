package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orders;
    public OrderController(OrderService orders) { this.orders = orders; }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody CreateOrderRequest request,
            @RequestHeader("Idempotency-Key") String key, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orders.createOrder(auth.getName(), key, request));
    }

    @GetMapping
    public List<OrderDTO> getUserOrders(Authentication auth) {
        return orders.getUserOrders(auth.getName());
    }

    @GetMapping("/{id}")
    public OrderDTO getOrderById(@PathVariable Long id, Authentication auth) {
        return orders.getOrderById(id, auth.getName());
    }

    @PutMapping("/{id}/status")
    public OrderDTO updateOrderStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return orders.updateOrderStatus(id, request.status());
    }

    public record StatusUpdateRequest(@NotNull OrderStatus status) {}
}
