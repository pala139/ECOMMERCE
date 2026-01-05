package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UserService handles user profile operations.
 * Why this class exists: Separate user management from authentication.
 * Single Responsibility: User profile CRUD operations.
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get user by email (usually called after JWT validation).
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    /**
     * Update user profile (address, phone, etc).
     */
    public User updateUserProfile(String email, User updates) {
        User user = getUserByEmail(email);
        
        // Update only provided fields
        if (updates.getName() != null) {
            user.setName(updates.getName());
        }
        if (updates.getPhoneNumber() != null) {
            user.setPhoneNumber(updates.getPhoneNumber());
        }
        if (updates.getAddress() != null) {
            user.setAddress(updates.getAddress());
        }
        if (updates.getCity() != null) {
            user.setCity(updates.getCity());
        }
        if (updates.getState() != null) {
            user.setState(updates.getState());
        }
        if (updates.getZipCode() != null) {
            user.setZipCode(updates.getZipCode());
        }
        
        return userRepository.save(user);
    }
}
