package com.ecommerce.dto;
import com.ecommerce.entity.User;
public record UserProfile(Long id, String email, String name, String phoneNumber,
        String address, String city, String state, String zipCode) {
    public static UserProfile from(User u) {
        return new UserProfile(u.getId(), u.getEmail(), u.getName(), u.getPhoneNumber(),
            u.getAddress(), u.getCity(), u.getState(), u.getZipCode());
    }
}
