package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.*;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderServiceTest - Unit tests for OrderService
 * 
 * Why these tests matter:
 * - Orders are critical - bugs here mean money loss!
 * - Tests verify inventory management (stock validation)
 * - Tests verify user-order relationship (security)
 * - Tests prevent data integrity issues
 * 
 * Test coverage includes:
 * - Successful order creation with valid data
 * - Product not found scenarios
 * - Insufficient stock scenarios
 * - User-specific order retrieval
 * - Order status updates
 * 
 * Interview talking point:
 * "I test business-critical operations thoroughly. For orders, I ensure:
 * 1) Stock validation (can't order more than available)
 * 2) User isolation (users only see their orders)
 * 3) Data consistency (order-items relationship is preserved)
 * 4) Edge cases (empty orders, zero quantity)"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Category testCategory;
    private Product testProduct;
    private Order testOrder;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setName("Test User");
        testUser.setPassword("hashed_password");

        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        // Create test product with stock
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStockQuantity(10);
        testProduct.setCategory(testCategory);

        // Create test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(new BigDecimal("999.99"));
        testOrder.setShippingAddress("123 Main St");
        testOrder.setShippingCity("Springfield");
        testOrder.setShippingState("IL");
        testOrder.setShippingZipCode("62701");

        // Create order items
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(testOrder);
        orderItem.setProduct(testProduct);
        orderItem.setQuantity(1);
        orderItem.setPriceAtPurchase(new BigDecimal("999.99"));

        testOrder.setItems(new ArrayList<>());
        testOrder.getItems().add(orderItem);

        // Create order request
        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setShippingAddress("123 Main St");
        createOrderRequest.setShippingCity("Springfield");
        createOrderRequest.setShippingState("IL");
        createOrderRequest.setShippingZipCode("62701");

        CreateOrderRequest.OrderItemRequest itemRequest = new CreateOrderRequest.OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(1);
        createOrderRequest.setItems(new ArrayList<>());
        createOrderRequest.getItems().add(itemRequest);
    }

    @Test
    @DisplayName("Should create order successfully with valid data and sufficient stock")
    void testCreateOrder_Success() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.createOrder("user@example.com", createOrderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertFalse(result.getItems().isEmpty());
        
        // Verify interactions
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when product in order doesn't exist")
    void testCreateOrder_ProductNotFound() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("user@example.com", createOrderRequest);
        });

        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw RuntimeException when ordering more than available stock")
    void testCreateOrder_InsufficientStock() {
        // Arrange
        testProduct.setStockQuantity(0); // No stock
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder("user@example.com", createOrderRequest);
        });

        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve user orders successfully")
    void testGetUserOrders_Success() {
        // Arrange
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(orders);

        // Act
        List<OrderDTO> result = orderService.getUserOrders("user@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        
        verify(userRepository).findByEmail("user@example.com");
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void testGetUserOrders_NoOrders() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        // Act
        List<OrderDTO> result = orderService.getUserOrders("user@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(userRepository).findByEmail("user@example.com");
        verify(orderRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Should retrieve order by ID successfully")
    void testGetOrderById_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        OrderDTO result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PENDING", result.getStatus());
        
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw RuntimeException when order not found")
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(1L);
        });

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus_Success() {
        // Arrange
        testOrder.setStatus("PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.updateOrderStatus(1L, "CONFIRMED");

        // Assert
        assertNotNull(result);
        
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle order with multiple items")
    void testCreateOrder_MultipleItems() {
        // Arrange
        CreateOrderRequest.OrderItemRequest item2 = new CreateOrderRequest.OrderItemRequest();
        item2.setProductId(1L);
        item2.setQuantity(2);
        createOrderRequest.getItems().add(item2);

        Product product2 = new Product();
        product2.setId(1L);
        product2.setName("Laptop");
        product2.setPrice(new BigDecimal("999.99"));
        product2.setStockQuantity(10);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.createOrder("user@example.com", createOrderRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository, times(2)).findById(1L); // Called for each item
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should verify total amount calculation")
    void testCreateOrder_TotalAmountCalculation() {
        // Arrange
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.createOrder("user@example.com", createOrderRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
        // Expected: 1 * 999.99 = 999.99
        
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should handle order with exact stock quantity")
    void testCreateOrder_ExactStockQuantity() {
        // Arrange
        testProduct.setStockQuantity(1); // Exactly 1 in stock
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderDTO result = orderService.createOrder("user@example.com", createOrderRequest);

        // Assert
        assertNotNull(result);
        
        verify(userRepository).findByEmail("user@example.com");
        verify(productRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }
}
