package com.ecommerce.controller;

import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController handles authentication endpoints.
 * Why this class exists: Expose authentication operations via REST API.
 * Single Responsibility: HTTP request/response handling for auth.
 * 
 * Request flow:
 * 1. POST /auth/register - Create new user account
 * 2. POST /auth/login - Get JWT token
 * 
 * Interview talking point:
 * - Controllers are thin - delegate business logic to services
 * - Conversion from request to response happens here
 * - HTTP status codes matter: 200 OK, 201 CREATED, 400 Bad Request, 401 Unauthorized
 * - @Valid triggers validation on request DTOs before business logic
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Allow requests from frontend (adjust in production)
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * Register new user.
     * Request: { email, password, name, phoneNumber }
     * Response: { token, email, name, userId }
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Login with email and password.
     * Request: { email, password }
     * Response: { token, email, name, userId }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
