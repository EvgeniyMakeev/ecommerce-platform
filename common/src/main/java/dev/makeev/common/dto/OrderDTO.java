package dev.makeev.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDTO(
        String id,
        String userId,
        List<OrderItemDTO> items,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt) {
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        SHIPPED,
        DELIVERED,
        CANCELLED
    }
}
