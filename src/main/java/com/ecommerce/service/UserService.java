package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.dto.UpdateProfileRequest;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUserProfile(String email, UpdateProfileRequest updates) {
        User user = getUserByEmail(email);
        
        // Update only provided fields
        if (updates.name() != null) {
            user.setName(updates.name());
        }
        if (updates.phoneNumber() != null) {
            user.setPhoneNumber(updates.phoneNumber());
        }
        if (updates.address() != null) {
            user.setAddress(updates.address());
        }
        if (updates.city() != null) {
            user.setCity(updates.city());
        }
        if (updates.state() != null) {
            user.setState(updates.state());
        }
        if (updates.zipCode() != null) {
            user.setZipCode(updates.zipCode());
        }
        
        return userRepository.save(user);
    }
}
