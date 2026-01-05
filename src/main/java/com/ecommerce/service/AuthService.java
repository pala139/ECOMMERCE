package com.ecommerce.service;

import com.ecommerce.dto.AuthResponse;
import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService handles user authentication (login and registration).
 * Why this class exists: Centralize authentication logic away from controllers.
 * Single Responsibility: Handle user signup and login.
 * 
 * Request flow for login:
 * 1. Client sends email and password
 * 2. Service finds user in database by email
 * 3. Service compares provided password with stored (encrypted) password
 * 4. If match, generate JWT token
 * 5. Return token to client
 * 
 * For registration:
 * 1. Client sends registration details
 * 2. Check if user already exists
 * 3. Hash password using BCrypt
 * 4. Save new user to database
 * 5. Return JWT token so they're immediately logged in
 * 
 * Interview talking point:
 * - Why hash password? Database breach won't expose raw passwords
 * - Why use BCrypt? Slow hashing prevents brute force attacks
 * - Why throw exception on duplicate email? Unique constraint in database prevents race conditions
 */
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Register a new user.
     * Throws exception if email already exists.
     */
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        
        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPhoneNumber(request.getPhoneNumber());
        
        // Hash password before saving - never store raw password
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Save to database
        user = userRepository.save(user);
        
        // Generate token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Return response with token
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }
    
    /**
     * Authenticate user with email and password.
     * Returns JWT token if credentials are correct.
     */
    public AuthResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Compare provided password with stored (hashed) password
        // passwordEncoder.matches() handles comparing raw password with BCrypt hash
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Return response
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }
}
