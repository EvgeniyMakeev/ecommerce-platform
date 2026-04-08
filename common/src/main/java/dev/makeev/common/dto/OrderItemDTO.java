package dev.makeev.common.dto;

import java.math.BigDecimal;

public record OrderItemDTO(
        String productId,
        String productName,
        Integer quantity,
        BigDecimal price) {
}
