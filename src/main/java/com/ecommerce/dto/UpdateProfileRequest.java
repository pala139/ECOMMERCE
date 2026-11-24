package com.ecommerce.dto;
import jakarta.validation.constraints.Size;
public record UpdateProfileRequest(@Size(min=2,max=100) String name,
        @Size(max=20) String phoneNumber, @Size(max=200) String address,
        @Size(max=100) String city, @Size(max=50) String state,
        @Size(max=10) String zipCode) {}
