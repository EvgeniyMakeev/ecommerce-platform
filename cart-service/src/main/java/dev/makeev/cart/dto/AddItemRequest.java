package dev.makeev.cart.dto;

import java.math.BigDecimal;

public record AddItemRequest(
        String productId,
        String productName,
        BigDecimal price,
        int quantity,
        String imageUrl) {
}
