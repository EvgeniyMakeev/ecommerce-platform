package dev.makeev.common.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ProductDTO(
        String id,
        String name,
        String description,
        BigDecimal price,
        String category,
        List<String> tags,
        String imageUrl,
        Instant createdAt,
        Instant updatedAt) {
}
