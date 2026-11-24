package com.ecommerce.dto;

import com.ecommerce.entity.Category;

public record CategoryDTO(Long id, String name, String description) {
    public static CategoryDTO from(Category c) {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription());
    }
}
