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
@Table("user_activities")
public class UserActivity {
    
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
    
    @Column("activity_type")
    private ActivityType activityType;
    
    @Column("session_id")
    private String sessionId;
    
    @Column("duration_seconds")
    private Integer durationSeconds;
    
    @Column("price")
    private Double price;
    
    @Column("quantity")
    private Integer quantity;
    
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("context")
    private String context;

}
