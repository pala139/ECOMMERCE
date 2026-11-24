package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.ecommerce.exception.ApiException;

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
    public PageResponse<OrderDTO> getUserOrders(Authentication auth,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid pagination");
        return PageResponse.from(orders.getUserOrders(auth.getName(), PageRequest.of(page, size,
            Sort.by(Sort.Direction.DESC, "id"))));
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
