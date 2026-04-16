package dev.makeev.search.model;

import dev.makeev.common.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class ProductDocument {

    @Field(type = FieldType.Keyword)
    private String id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Text)
    private String description;
    @Field(type = FieldType.Double)
    private BigDecimal price;
    @Field(type = FieldType.Keyword)
    private String category;
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    @Field(type = FieldType.Keyword)
    private String imageUrl;
    @Field(type = FieldType.Date)
    private Instant createdAt;
    @Field(type = FieldType.Date)
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
