package com.ecommerce.controller;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.service.AuthService;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ValidationIntegrationTest - Test that validation annotations work correctly
 * 
 * Why integration tests matter:
 * - Unit tests mock everything; integration tests use real Spring context
 * - Validates that @Valid annotation triggers properly on controller
 * - Tests the full validation chain: DTO -> Controller -> GlobalExceptionHandler -> Response
 * - Ensures error messages are returned properly to frontend
 * 
 * What this test does:
 * 1. Send invalid DTOs to API endpoints
 * 2. Verify 400 Bad Request is returned (not 200 or 500)
 * 3. Verify error messages are descriptive (helps frontend show useful messages)
 * 4. Verify validation happens at controller boundary (not in business logic)
 * 
 * Interview talking point:
 * "I test validation at the integration level to ensure bad requests are 
 * rejected early with meaningful error messages. This prevents invalid data 
 * from reaching the business logic layer."
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Validation Integration Tests")
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private ProductService productService;

    // ============================================
    // LOGIN REQUEST VALIDATION TESTS
    // ============================================

    @Test
    @DisplayName("Should reject login with blank email - 400 Bad Request")
    void testLoginValidation_BlankEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail(""); // Empty email - should fail
        request.setPassword("ValidPassword123!");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should reject login with invalid email format - 400 Bad Request")
    void testLoginValidation_InvalidEmailFormat() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email"); // Invalid format
        request.setPassword("ValidPassword123!");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject login with blank password - 400 Bad Request")
    void testLoginValidation_BlankPassword() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword(""); // Empty password - should fail

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================
    // REGISTER REQUEST VALIDATION TESTS
    // ============================================

    @Test
    @DisplayName("Should reject registration with password less than 8 characters")
    void testRegisterValidation_ShortPassword() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Short1!"); // Only 7 characters
        request.setName("Test User");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with password missing uppercase")
    void testRegisterValidation_NoUppercasePassword() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("lowercase123!"); // No uppercase
        request.setName("Test User");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with password missing special character")
    void testRegisterValidation_NoSpecialCharPassword() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123"); // No special character
        request.setName("Test User");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with name too short")
    void testRegisterValidation_ShortName() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        request.setName("A"); // Too short (min 2)

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject registration with invalid phone number")
    void testRegisterValidation_InvalidPhoneNumber() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        request.setName("Test User");
        request.setPhoneNumber("123"); // Invalid - not 10 digits

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================
    // CREATE PRODUCT REQUEST VALIDATION TESTS
    // ============================================

    @Test
    @DisplayName("Should reject product creation with blank name")
    void testCreateProductValidation_BlankName() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName(""); // Blank name
        request.setDescription("A laptop");
        request.setPrice(new BigDecimal("999.99"));
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject product creation with null price")
    void testCreateProductValidation_NullPrice() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(null); // Null price
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject product creation with negative price")
    void testCreateProductValidation_NegativePrice() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(new BigDecimal("-100")); // Negative price
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject product creation with negative stock quantity")
    void testCreateProductValidation_NegativeStock() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(new BigDecimal("999.99"));
        request.setStockQuantity(-5); // Negative stock
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject product creation with null category ID")
    void testCreateProductValidation_NullCategoryId() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(new BigDecimal("999.99"));
        request.setStockQuantity(10);
        request.setCategoryId(null); // Null category

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject product creation with name exceeding max length")
    void testCreateProductValidation_NameTooLong() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("A".repeat(201)); // Exceeds max 200 characters
        request.setDescription("A laptop");
        request.setPrice(new BigDecimal("999.99"));
        request.setStockQuantity(10);
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ============================================
    // VALIDATION SUCCESS CASES
    // ============================================

    @Test
    @DisplayName("Should accept valid login request")
    void testLoginValidation_ValidRequest() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("ValidPassword123!");

        // Act & Assert
        // Note: Will return unauthorized (user doesn't exist in mock) but passes validation
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // Not 400, validation passed
    }

    @Test
    @DisplayName("Should accept valid registration request")
    void testRegisterValidation_ValidRequest() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("ValidPassword123!");
        request.setName("New User");

        // Act & Assert
        // Will return 201 or error based on service, but not 400 validation error
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Expect 201 CREATED
    }

    @Test
    @DisplayName("Should accept valid product creation request")
    void testCreateProductValidation_ValidRequest() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Valid Product");
        request.setDescription("A valid product");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(5);
        request.setCategoryId(1L);

        // Act & Assert
        // Will return 201 or error based on service, but not 400 validation error
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Expect 201 CREATED
    }

    // ============================================
    // EDGE CASE VALIDATION TESTS
    // ============================================

    @Test
    @DisplayName("Should accept minimum valid name length (2 characters)")
    void testRegisterValidation_MinimumNameLength() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        request.setName("AB"); // Exactly 2 characters (minimum)

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Validation should pass, 201 expected
    }

    @Test
    @DisplayName("Should accept zero stock quantity for out-of-stock products")
    void testCreateProductValidation_ZeroStock() throws Exception {
        // Arrange
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Out of Stock Product");
        request.setDescription("Product with no stock");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(0); // Zero is valid (means out of stock)
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Zero stock should be valid, 201 expected
    }
}
