package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import com.ecommerce.service.OrderService;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ApiException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ValidationIntegrationTest {
    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");
    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("jwt.secret", () -> "test-only-secret-at-least-32-bytes-123456");
    }

    @Autowired MockMvc mvc;
    @Autowired OrderService service;
    @Autowired ProductRepository products;
    @Autowired UserRepository users;
    @Autowired com.ecommerce.util.JwtUtil jwt;

    @Test void protectedEndpointsRejectAnonymousRequests() throws Exception {
        mvc.perform(get("/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/users/profile")).andExpect(status().isUnauthorized());
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
    }
    @Test void invalidRegistrationReturnsBadRequest() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
    }
    @Test void customerCannotCreateProductOrReadAnotherOrder() throws Exception {
        User user = user();
        String token = "Bearer " + jwt.generateToken(user.getEmail());
        mvc.perform(post("/products").header("Authorization",token)
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/users/profile").header("Authorization",token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        User owner = user();
        var order = service.createOrder(owner.getEmail(),"key",request(product(1).getId(),1));
        mvc.perform(get("/orders/"+order.getId()).header("Authorization",token)).andExpect(status().isNotFound());
        mvc.perform(put("/orders/"+order.getId()+"/status").header("Authorization",token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELLED\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void adminCanCreateCategoryAndValidationWorks() throws Exception {
        User admin = user(); admin.setRole(Role.ADMIN); users.save(admin);
        String token = "Bearer " + jwt.generateToken(admin.getEmail());
        mvc.perform(post("/categories").header("Authorization",token)
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Books\"}"))
            .andExpect(status().isCreated());
        mvc.perform(post("/products").header("Authorization",token)
            .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }
    @Test void concurrentOrdersCannotOversell() throws Exception {
        Product product = product(1);
        User a = user(), b = user();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Boolean> first = pool.submit(() -> buy(start,a,product));
            Future<Boolean> second = pool.submit(() -> buy(start,b,product));
            start.countDown();
            assertNotEquals(first.get(20,TimeUnit.SECONDS),second.get(20,TimeUnit.SECONDS));
            assertEquals(0,products.findById(product.getId()).orElseThrow().getStockQuantity());
        } finally { pool.shutdownNow(); }
    }
    @Test void failedCheckoutRollsBackAndRetryIsIdempotent() {
        User user = user();
        Product a = product(2), b = product(0);
        var invalid = request(a.getId(),1);
        invalid.setItems(List.of(new CreateOrderRequest.OrderItemRequest(a.getId(),1),
            new CreateOrderRequest.OrderItemRequest(b.getId(),1)));
        assertThrows(InsufficientStockException.class, () -> service.createOrder(user.getEmail(),"failed",invalid));
        assertEquals(2,products.findById(a.getId()).orElseThrow().getStockQuantity());
        var valid = request(a.getId(),1);
        var first = service.createOrder(user.getEmail(),"retry",valid);
        var second = service.createOrder(user.getEmail(),"retry",valid);
        assertEquals(first.getId(),second.getId());
        assertThrows(ApiException.class, () -> service.createOrder(user.getEmail(),"retry",request(a.getId(),2)));
        assertEquals(1,products.findById(a.getId()).orElseThrow().getStockQuantity());
        service.updateOrderStatus(first.getId(),OrderStatus.CANCELLED);
        service.updateOrderStatus(first.getId(),OrderStatus.CANCELLED);
        assertEquals(2,products.findById(a.getId()).orElseThrow().getStockQuantity());
    }
    boolean buy(CountDownLatch start, User user, Product product) throws InterruptedException {
        start.await();
        try { service.createOrder(user.getEmail(),"key",request(product.getId(),1)); return true; }
        catch (InsufficientStockException ex) { return false; }
    }
    User user() {
        User u = new User(); u.setEmail(UUID.randomUUID()+"@example.com"); u.setName("Test");
        u.setPassword("unused-test-hash"); return users.save(u);
    }
    Product product(int stock) {
        Product p = new Product(); p.setName("Test product"); p.setPrice(new BigDecimal("10.00"));
        p.setStockQuantity(stock); return products.save(p);
    }
    CreateOrderRequest request(Long id,int quantity) {
        return new CreateOrderRequest(List.of(new CreateOrderRequest.OrderItemRequest(id,quantity)),
            "123 Main Street","Hyderabad","Telangana","500001");
    }
}
