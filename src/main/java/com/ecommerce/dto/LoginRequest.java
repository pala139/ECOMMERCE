package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: Data Transfer Object
 * Why we use DTOs: Control what data is sent to/from API. Don't expose internal database details.
 * This LoginRequest only needs email and password - we don't send the User entity.
 * 
 * Validation: @NotBlank ensures field is not empty/null, @Email validates email format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    private String password;
}

