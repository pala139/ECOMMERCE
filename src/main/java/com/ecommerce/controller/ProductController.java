package com.ecommerce.controller;

import com.ecommerce.dto.*;
import com.ecommerce.exception.ApiException;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService products;
    public ProductController(ProductService products) { this.products = products; }

    @GetMapping
    public PageResponse<ProductDTO> list(@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size, @RequestParam(defaultValue="id") String sort,
            @RequestParam(defaultValue="asc") String direction,
            @RequestParam(required=false) String keyword, @RequestParam(required=false) Long categoryId) {
        if (page < 0 || size < 1 || size > 100 || !Set.of("id","name","price").contains(sort)
                || !Set.of("asc","desc").contains(direction)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid pagination or sort");
        }
        Sort ordering = Sort.by(Sort.Direction.fromString(direction), sort);
        if (!sort.equals("id")) ordering = ordering.and(Sort.by("id"));
        Page<ProductDTO> result = products.findProducts(keyword, categoryId, PageRequest.of(page, size, ordering));
        return PageResponse.from(result);
    }
    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable Long id) { return products.getProductById(id); }
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(products.createProduct(request));
    }
}
