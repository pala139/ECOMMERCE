package com.ecommerce.controller;
import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categories;
    public CategoryController(CategoryService categories) { this.categories = categories; }
    @GetMapping public List<Category> list() { return categories.list(); }
    @GetMapping("/{id}") public Category get(@PathVariable Long id) { return categories.get(id); }
    @PostMapping public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categories.create(request));
    }
}
