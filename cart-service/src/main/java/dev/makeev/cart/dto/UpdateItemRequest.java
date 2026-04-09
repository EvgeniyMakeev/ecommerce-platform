package dev.makeev.cart.dto;

import java.math.BigDecimal;

public record UpdateItemRequest(
        String productName,
        BigDecimal price,
        int quantity,
        String imageUrl) {
}
