package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final UserRepository users;
    private final ObjectMapper mapper;

    public OrderService(OrderRepository orders, ProductRepository products,
            UserRepository users, ObjectMapper mapper) {
        this.orders = orders;
        this.products = products;
        this.users = users;
        this.mapper = mapper;
    }

    @Transactional
    public OrderDTO createOrder(String email, String key, CreateOrderRequest request) {
        if (key == null || key.isBlank() || key.length() > 128) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Idempotency-Key must contain 1-128 characters");
        }
        // Serialize retries for one customer; product locks protect stock across customers.
        User user = users.findForCheckout(email).orElseThrow(() -> notFound("User"));
        String hash = requestHash(request);
        Optional<Order> previous = orders.findByUserIdAndIdempotencyKey(user.getId(), key);
        if (previous.isPresent()) {
            if (!hash.equals(previous.get().getRequestHash())) {
                throw new ApiException(HttpStatus.CONFLICT, "Idempotency key already used for a different request");
            }
            return toDto(previous.get());
        }

        // Merge duplicate items and acquire locks in ascending product ID order.
        Map<Long, Integer> quantities = new TreeMap<>();
        for (var item : request.getItems()) {
            quantities.merge(item.getProductId(), item.getQuantity(), Math::addExact);
        }
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-" + UUID.randomUUID());
        order.setIdempotencyKey(key);
        order.setRequestHash(hash);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZipCode(request.getShippingZipCode());

        BigDecimal total = BigDecimal.ZERO;
        for (var entry : quantities.entrySet()) {
            Product product = products.findForUpdate(entry.getKey()).orElseThrow(() -> notFound("Product"));
            int quantity = entry.getValue();
            if (product.getStockQuantity() < quantity) {
                throw new InsufficientStockException(product.getName(), quantity, product.getStockQuantity());
            }
            product.setStockQuantity(product.getStockQuantity() - quantity);
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setPriceAtPurchase(product.getPrice());
            order.getItems().add(item);
            total = total.add(item.getSubtotal());
        }
        order.setTotalAmount(total);
        return toDto(orders.saveAndFlush(order));
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id, String email) {
        return toDto(orders.findByIdAndUserEmail(id, email).orElseThrow(() -> notFound("Order")));
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderDTO> getUserOrders(String email, org.springframework.data.domain.Pageable page) {
        User user = users.findByEmail(email).orElseThrow(() -> notFound("User"));
        return orders.findByUserId(user.getId(), page).map(this::toDto);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus next) {
        Order order = orders.findForUpdate(id).orElseThrow(() -> notFound("Order"));
        OrderStatus current = OrderStatus.valueOf(order.getStatus());
        if (current == next) return toDto(order);
        if (!current.canTransitionTo(next)) {
            throw new ApiException(HttpStatus.CONFLICT, "Invalid order status transition");
        }
        if (next == OrderStatus.CANCELLED) {
            order.getItems().stream().sorted(Comparator.comparing(i -> i.getProduct().getId())).forEach(item -> {
                Product product = products.findForUpdate(item.getProduct().getId()).orElseThrow(() -> notFound("Product"));
                product.setStockQuantity(Math.addExact(product.getStockQuantity(), item.getQuantity()));
            });
        }
        order.setStatus(next.name());
        return toDto(order);
    }

    private String requestHash(CreateOrderRequest request) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(mapper.writeValueAsBytes(request)));
        } catch (NoSuchAlgorithmException | JsonProcessingException ex) {
            throw new IllegalStateException("Could not fingerprint order request", ex);
        }
    }

    private ApiException notFound(String resource) {
        return new ApiException(HttpStatus.NOT_FOUND, resource + " not found");
    }

    private OrderDTO toDto(Order order) {
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
        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemDTO line = new OrderItemDTO();
            line.setId(item.getId());
            line.setProductId(item.getProduct().getId());
            line.setProductName(item.getProduct().getName());
            line.setQuantity(item.getQuantity());
            line.setPriceAtPurchase(item.getPriceAtPurchase());
            line.setSubtotal(item.getSubtotal());
            return line;
        }).toList());
        return dto;
    }
}
