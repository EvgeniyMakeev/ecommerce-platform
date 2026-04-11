package dev.makeev.recommendation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PopularProductRequest(
        @NotBlank(message = "Product ID is required")
        String productId,
        
        @NotBlank(message = "Product name is required")
        String productName,
        
        String productDescription,
        
        @NotBlank(message = "Category is required")
        String category,
        
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        BigDecimal price,
        
        String imageUrl
) {}
