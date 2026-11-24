package com.ecommerce.service;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.entity.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock ProductRepository products;
    @Mock CategoryRepository categories;
    @InjectMocks ProductService service;
    Product product;
    Category category;

    @BeforeEach void setup() {
        category = new Category(); category.setId(1L); category.setName("Books");
        product = new Product(); product.setId(1L); product.setName("Book");
        product.setPrice(new BigDecimal("10.00")); product.setStockQuantity(5); product.setCategory(category);
    }
    @Test void mapsPageAndCategory() {
        Pageable page = PageRequest.of(0, 20);
        when(products.findAll(page)).thenReturn(new PageImpl<>(List.of(product),page,1));
        var result = service.findProducts(null,null,page);
        assertEquals(1,result.getTotalElements());
        assertEquals("Books",result.getContent().get(0).getCategoryName());
    }
    @Test void searchesWithBoundedPage() {
        Pageable page = PageRequest.of(0,10);
        when(products.findByNameContainingIgnoreCase("book",page)).thenReturn(Page.empty(page));
        assertTrue(service.findProducts("book",null,page).isEmpty());
    }
    @Test void createsProduct() {
        CreateProductRequest request = new CreateProductRequest("Book",null,new BigDecimal("10.00"),5,1L);
        when(categories.findById(1L)).thenReturn(Optional.of(category));
        when(products.save(any())).thenReturn(product);
        assertEquals("Book",service.createProduct(request).getName());
    }
    @Test void missingProductReturnsNotFound() {
        when(products.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> service.getProductById(1L));
    }
}
