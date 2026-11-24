package com.ecommerce.service;

import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.exception.ApiException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;
    private final CategoryRepository categories;
    public ProductService(ProductRepository products, CategoryRepository categories) {
        this.products = products;
        this.categories = categories;
    }

    public Page<ProductDTO> findProducts(String keyword, Long categoryId, Pageable page) {
        if (keyword != null && categoryId != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose keyword or categoryId");
        }
        if (keyword != null) return products.findByNameContainingIgnoreCase(keyword, page).map(this::toDto);
        if (categoryId != null) return products.findByCategoryId(categoryId, page).map(this::toDto);
        return products.findAll(page).map(this::toDto);
    }

    public ProductDTO getProductById(Long id) {
        return toDto(products.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "Product not found")));
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        var category = categories.findById(request.getCategoryId()).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        return toDto(products.save(product));
    }

    private ProductDTO toDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        return dto;
    }
}
