package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.entity.*;
import com.ecommerce.exception.*;
import com.ecommerce.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock OrderRepository orders;
    @Mock ProductRepository products;
    @Mock UserRepository users;
    OrderService service;
    User user;
    Product product;

    @BeforeEach void setup() {
        service = new OrderService(orders, products, users, new ObjectMapper());
        user = new User(); user.setId(1L);
        product = new Product(); product.setId(1L); product.setName("Book");
        product.setPrice(new BigDecimal("10.00")); product.setStockQuantity(5);
    }
    CreateOrderRequest request(int quantity) {
        return new CreateOrderRequest(List.of(new CreateOrderRequest.OrderItemRequest(1L, quantity)),
            "123 Main Street","Hyderabad","Telangana","500001");
    }
    @Test void reservesStockAndFreezesPrice() {
        when(users.findForCheckout("a@example.com")).thenReturn(Optional.of(user));
        when(products.findForUpdate(1L)).thenReturn(Optional.of(product));
        when(orders.saveAndFlush(any())).thenAnswer(i -> i.getArgument(0));
        var result = service.createOrder("a@example.com","key",request(3));
        assertEquals(2, product.getStockQuantity());
        assertEquals(new BigDecimal("30.00"),result.getTotalAmount());
        assertEquals("PENDING",result.getStatus());
    }
    @Test void rejectsInsufficientStock() {
        when(users.findForCheckout("a@example.com")).thenReturn(Optional.of(user));
        when(products.findForUpdate(1L)).thenReturn(Optional.of(product));
        assertThrows(InsufficientStockException.class, () -> service.createOrder("a@example.com","key",request(6)));
        verify(orders,never()).saveAndFlush(any());
    }
    @Test void cannotReadAnotherCustomersOrder() {
        when(orders.findByIdAndUserEmail(1L,"other@example.com")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.getOrderById(1L,"other@example.com"));
    }
    @Test void rejectsInvalidTransition() {
        Order order = new Order(); order.setStatus("DELIVERED");
        when(orders.findForUpdate(1L)).thenReturn(Optional.of(order));
        assertThrows(ApiException.class, () -> service.updateOrderStatus(1L,OrderStatus.PENDING));
    }
}
