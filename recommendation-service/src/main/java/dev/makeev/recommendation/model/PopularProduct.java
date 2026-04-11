package dev.makeev.recommendation.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("popular_products")
public class PopularProduct {
    
    @Id
    private Long id;
    
    @Column("product_id")
    private String productId;
    
    @Column("product_name")
    private String productName;
    
    @Column("product_description")
    private String productDescription;
    private String category;
    private BigDecimal price;
    
    @Column("image_url")
    private String imageUrl;
    
    @Column("view_count")
    private Long viewCount;
    
    @Column("purchase_count")
    private Long purchaseCount;
    private Double rating;
    
    @Column("popularity_score")
    private Double popularityScore;
    
    @Column("last_updated")
    private LocalDateTime lastUpdated;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    public PopularProduct(String productId, String productName, String productDescription, 
                         String category, BigDecimal price, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.viewCount = 0L;
        this.purchaseCount = 0L;
        this.rating = 0.0;
        this.popularityScore = 0.0;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }
    
    public void incrementViewCount() {
        this.viewCount++;
        this.lastUpdated = LocalDateTime.now();
        calculatePopularityScore();
    }
    
    public void incrementPurchaseCount() {
        this.purchaseCount++;
        this.lastUpdated = LocalDateTime.now();
        calculatePopularityScore();
    }
    
    public void updateRating(Double newRating) {
        this.rating = newRating;
        this.lastUpdated = LocalDateTime.now();
        calculatePopularityScore();
    }
    
    private void calculatePopularityScore() {
        double viewScore = Math.log1p(viewCount) * 0.4;
        double purchaseScore = Math.log1p(purchaseCount) * 0.4;
        double ratingScore = (rating / 5.0) * 0.2;
        
        this.popularityScore = viewScore + purchaseScore + ratingScore;
    }
}
