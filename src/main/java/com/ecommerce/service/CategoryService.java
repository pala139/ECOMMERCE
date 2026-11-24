package com.ecommerce.service;
import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    public CategoryService(CategoryRepository categories) { this.categories = categories; }
    public List<Category> list() { return categories.findAll(); }
    public Category get(Long id) {
        return categories.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Category not found"));
    }
    public Category create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return categories.save(category);
    }
}
