package dev.makeev.search.model;

import dev.makeev.common.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
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
        ProductDocument doc = new ProductDocument();
        doc.id = productDTO.id();
        doc.name = productDTO.name();
        doc.description = productDTO.description();
        doc.price = productDTO.price();
        doc.category = productDTO.category();
        doc.tags = productDTO.tags();
        doc.imageUrl = productDTO.imageUrl();
        doc.createdAt = productDTO.createdAt();
        doc.updatedAt = productDTO.updatedAt();
        return doc;
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
