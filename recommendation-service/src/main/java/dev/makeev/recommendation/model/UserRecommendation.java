package dev.makeev.recommendation.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_recommendations")
public class UserRecommendation {
    
    @Id
    private Long id;
    
    @Column("user_id")
    private String userId;
    
    @Column("product_id")
    private String productId;
    
    @Column("product_name")
    private String productName;
    
    @Column("category")
    private String category;
    
    @Column("price")
    private Double price;
    
    @Column("image_url")
    private String imageUrl;
    
    @Column("recommendation_type")
    private RecommendationType recommendationType;
    
    @Column("recommendation_score")
    private Double recommendationScore;
    
    @Column("reason")
    private String reason;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("expires_at")
    private LocalDateTime expiresAt;
    
    @Column("clicked")
    private Boolean clicked = false;
    
    @Column("clicked_at")
    private LocalDateTime clickedAt;
    
    @Column("purchased")
    private Boolean purchased = false;
    
    @Column("purchased_at")
    private LocalDateTime purchasedAt;
    
    
    
    public void markAsClicked() {
        this.clicked = true;
        this.clickedAt = LocalDateTime.now();
    }
    
    public void markAsPurchased() {
        this.purchased = true;
        this.purchasedAt = LocalDateTime.now();
    }
}
