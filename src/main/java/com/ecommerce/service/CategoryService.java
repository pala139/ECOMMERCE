package com.ecommerce.service;

import com.ecommerce.dto.CategoryDTO;
import com.ecommerce.dto.CategoryRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.exception.ApiException;
import com.ecommerce.repository.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final CategoryCache cache;

    public CategoryService(CategoryRepository categories, CategoryCache cache) {
        this.categories = categories;
        this.cache = cache;
    }

    public List<CategoryDTO> list() {
        return cache.get().orElseGet(() -> {
            List<CategoryDTO> result = categories.findAll(Sort.by("name")).stream().map(CategoryDTO::from).toList();
            cache.put(result);
            return result;
        });
    }

    public CategoryDTO get(Long id) {
        return CategoryDTO.from(categories.findById(id).orElseThrow(() ->
            new ApiException(HttpStatus.NOT_FOUND, "Category not found")));
    }

    public CategoryDTO create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        CategoryDTO result = CategoryDTO.from(categories.save(category));
        cache.evict();
        return result;
    }
}
