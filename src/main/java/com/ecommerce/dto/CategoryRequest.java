package com.ecommerce.dto;
import jakarta.validation.constraints.*;
public record CategoryRequest(@NotBlank @Size(max=100) String name, @Size(max=255) String description) {}
