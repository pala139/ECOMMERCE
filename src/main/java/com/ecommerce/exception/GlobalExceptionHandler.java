package com.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> api(ApiException ex, WebRequest request) {
        return error(ex.getStatus(), ex.getMessage(), request);
    }
    @ExceptionHandler(InsufficientStockException.class)
    ResponseEntity<ErrorResponse> stock(InsufficientStockException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).sorted()
            .reduce((a,b) -> a + "; " + b).orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, ServletRequestBindingException.class,
        MethodArgumentTypeMismatchException.class, IllegalArgumentException.class, ArithmeticException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request", request);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> conflict(Exception ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, "Request conflicts with existing data", request);
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception ex, WebRequest request) {
        LOG.error("Unhandled request failure", ex);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(),
            status.value(), message, request.getDescription(false).replace("uri=", "")));
    }
}
