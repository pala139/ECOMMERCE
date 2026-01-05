package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;

/**
 * GlobalExceptionHandler catches exceptions from all controllers.
 * Why this class exists: Centralized exception handling avoids repeating try-catch in every controller.
 * Single Responsibility: Convert exceptions to proper HTTP responses.
 * 
 * Request flow on error:
 * 1. Controller throws RuntimeException
 * 2. @RestControllerAdvice intercepts it
 * 3. Matching @ExceptionHandler method is called
 * 4. Returns standardized error response
 * 5. Client gets JSON error with proper HTTP status
 * 
 * Interview talking point:
 * - Clean exception handling improves API usability
 * - Consistent error format helps frontend error handling
 * - Avoid exposing stack traces to clients (security)
 * - Different exceptions can have different HTTP status codes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle generic RuntimeException (business logic errors).
     * Example: "User not found", "Insufficient stock"
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle illegal argument (validation errors).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle unexpected exceptions (catch-all).
     * This prevents stack traces from leaking to clients.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "An unexpected error occurred",
            request.getDescription(false).replace("uri=", "")
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
