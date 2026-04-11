package dev.makeev.search.model;

import dev.makeev.common.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private List<String> tags;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductDocument from(ProductDTO productDTO) {
        return ProductDocument.builder()
                .id(productDTO.id())
                .name(productDTO.name())
                .description(productDTO.description())
                .price(productDTO.price())
                .category(productDTO.category())
                .tags(productDTO.tags())
                .imageUrl(productDTO.imageUrl())
                .createdAt(productDTO.createdAt())
                .updatedAt(productDTO.updatedAt())
                .build();
    }

    public ProductDTO toDTO() {
        return new ProductDTO(
                id,
                name,
                description,
                price,
                category,
                tags,
                imageUrl,
                createdAt,
                updatedAt
        );
    }
}
