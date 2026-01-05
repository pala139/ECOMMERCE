package com.ecommerce.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ErrorResponse - standardized error format for all API errors.
 * Why this class exists: Consistent error responses help frontend handle errors properly.
 * Example: { timestamp, status, message, path }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private String path;
}
