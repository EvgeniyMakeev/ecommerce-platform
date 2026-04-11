package dev.makeev.recommendation.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;

@Data
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
    
    
    public UserRecommendation(String userId, String productId, String productName, 
                             String category, Double price, String imageUrl,
                             RecommendationType recommendationType, 
                             Double recommendationScore, String reason) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.recommendationType = recommendationType;
        this.recommendationScore = recommendationScore;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7); // Recommendations expire in 7 days
    }
    
    public void markAsClicked() {
        this.clicked = true;
        this.clickedAt = LocalDateTime.now();
    }
    
    public void markAsPurchased() {
        this.purchased = true;
        this.purchasedAt = LocalDateTime.now();
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
