package com.ecommerce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RegisterRequest DTO for user registration.
 * Why separate from User entity: API users provide only necessary info, entity has timestamps etc.
 * 
 * Validation:
 * - @NotBlank: Field cannot be empty or null
 * - @Email: Must be valid email format
 * - @Size: Controls string length
 * - @Pattern: Regex pattern matching
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain uppercase, lowercase, digit, and special character"
    )
    private String password;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Pattern(
        regexp = "^[0-9]{10}$|^$",
        message = "Phone number must be 10 digits (optional)"
    )
    private String phoneNumber;
}

