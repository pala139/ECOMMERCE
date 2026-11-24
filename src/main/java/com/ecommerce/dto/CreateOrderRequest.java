package com.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    @Size(max = 100)
    private java.util.List<@NotNull OrderItemRequest> items;
    
    @NotBlank(message = "Shipping address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String shippingAddress;
    
    @NotBlank(message = "Shipping city is required")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String shippingCity;
    
    @NotBlank(message = "Shipping state is required")
    @Size(min = 2, max = 50, message = "State must be between 2 and 50 characters")
    private String shippingState;
    
    @NotBlank(message = "Shipping zip code is required")
    @Size(min = 5, max = 10, message = "Zip code must be between 5 and 10 characters")
    private String shippingZipCode;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        @Min(value = 1, message = "Product ID must be valid")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @jakarta.validation.constraints.Max(100000)
        private Integer quantity;
    }
}
