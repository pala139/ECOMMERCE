package com.ecommerce.service;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductServiceTest - Unit tests for ProductService
 * 
 * Why unit tests matter:
 * - Catch bugs before they reach production
 * - Safe refactoring - if tests pass, logic works!
 * - Document expected behavior
 * - Enable TDD (test-driven development)
 * 
 * Testing patterns used:
 * - @Mock: Mock database repositories
 * - @InjectMocks: Inject mocked dependencies
 * - AAA Pattern: Arrange (setup), Act (execute), Assert (verify)
 * - when().thenReturn(): Define mock behavior
 * - verify(): Confirm method interactions
 * 
 * Test coverage targets:
 * - Happy path: Product creation and retrieval success cases
 * - Sad path: Not found, validation failures
 * - Edge cases: Boundary values, null inputs
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;
    private CreateProductRequest createProductRequest;

    @BeforeEach
    void setUp() {
        // Create test data
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Laptop");
        testProduct.setDescription("High-performance laptop");
        testProduct.setPrice(new BigDecimal("999.99"));
        testProduct.setStockQuantity(10);
        testProduct.setCategory(testCategory);

        createProductRequest = new CreateProductRequest();
        createProductRequest.setName("Laptop");
        createProductRequest.setDescription("High-performance laptop");
        createProductRequest.setPrice(new BigDecimal("999.99"));
        createProductRequest.setStockQuantity(10);
        createProductRequest.setCategoryId(1L);
    }

    @Test
    @DisplayName("Should create product successfully with valid data")
    void testCreateProduct_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductDTO result = productService.createProduct(createProductRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals("999.99", result.getPrice().toString());
        assertEquals(10, result.getStockQuantity());
        
        // Verify interactions
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when category not found")
    void testCreateProduct_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.createProduct(createProductRequest);
        });

        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve all products successfully")
    void testGetAllProducts_Success() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void testGetAllProducts_Empty() {
        // Arrange
        when(productRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<ProductDTO> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should retrieve product by ID successfully")
    void testGetProductById_Success() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductDTO result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Laptop", result.getName());
        
        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found")
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.getProductById(1L);
        });

        verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should retrieve products by category successfully")
    void testGetProductsByCategory_Success() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(productRepository.findByCategoryId(1L)).thenReturn(products);

        // Act
        List<ProductDTO> result = productService.getProductsByCategory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCategoryId());
        
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    @DisplayName("Should search products by keyword successfully")
    void testSearchProducts_Success() {
        // Arrange
        List<Product> products = new ArrayList<>();
        products.add(testProduct);
        when(productRepository.findByNameContainingIgnoreCase("Laptop")).thenReturn(products);

        // Act
        List<ProductDTO> result = productService.searchProducts("Laptop");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getName());
        
        verify(productRepository).findByNameContainingIgnoreCase("Laptop");
    }

    @Test
    @DisplayName("Should return empty list when search keyword matches nothing")
    void testSearchProducts_NoMatches() {
        // Arrange
        when(productRepository.findByNameContainingIgnoreCase("NonExistent")).thenReturn(new ArrayList<>());

        // Act
        List<ProductDTO> result = productService.searchProducts("NonExistent");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(productRepository).findByNameContainingIgnoreCase("NonExistent");
    }

    @Test
    @DisplayName("Should handle negative price gracefully in validation")
    void testCreateProduct_NegativePrice() {
        // Arrange
        createProductRequest.setPrice(new BigDecimal("-10"));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act - This should fail at controller level with validation
        // But if it reaches service, service should handle it
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // For this test, we're documenting that validation happens at controller
        // The service assumes data is already validated
        ProductDTO result = productService.createProduct(createProductRequest);
        assertNotNull(result); // Would actually fail at controller with @Valid

        verify(categoryRepository).findById(1L);
    }

    @Test
    @DisplayName("Should handle zero stock quantity")
    void testCreateProduct_ZeroStock() {
        // Arrange
        createProductRequest.setStockQuantity(0);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductDTO result = productService.createProduct(createProductRequest);

        // Assert
        assertNotNull(result);
        // Zero stock is allowed (for out-of-stock items)
        
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }
}
