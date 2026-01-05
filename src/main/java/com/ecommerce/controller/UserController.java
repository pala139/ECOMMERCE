package com.ecommerce.controller;

import com.ecommerce.entity.User;
import com.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * UserController handles user profile operations.
 * Why this class exists: HTTP endpoint handler for user profiles.
 * Single Responsibility: Handle HTTP requests/responses for user data.
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * GET /users/profile - Get authenticated user's profile.
     */
    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        String userEmail = authentication.getPrincipal().toString();
        User user = userService.getUserByEmail(userEmail);
        return ResponseEntity.ok(user);
    }
    
    /**
     * PUT /users/profile - Update authenticated user's profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(
            @RequestBody User updates,
            Authentication authentication) {
        
        String userEmail = authentication.getPrincipal().toString();
        User updated = userService.updateUserProfile(userEmail, updates);
        return ResponseEntity.ok(updated);
    }
}
