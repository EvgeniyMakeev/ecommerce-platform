package dev.makeev.common.dto;

import java.math.BigDecimal;

public record CartItemDTO(
        String productId,
        String productName,
        Integer quantity,
        BigDecimal price) {
}
