package dev.makeev.recommendation.repository;

import dev.makeev.recommendation.model.RecommendationType;
import dev.makeev.recommendation.model.UserRecommendation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRecommendationRepository extends R2dbcRepository<UserRecommendation, Long> {
    
    Flux<UserRecommendation> findByUserId(String userId);
    
    Flux<UserRecommendation> findByUserIdAndRecommendationType(String userId, 
                                                              RecommendationType type);
    
    Flux<UserRecommendation> findByUserIdAndClickedFalseOrderByRecommendationScoreDesc(String userId);
    
    Flux<UserRecommendation> findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(
            String userId, LocalDateTime now);
    
    Flux<UserRecommendation> findTop10ByUserIdAndClickedFalseOrderByRecommendationScoreDesc(String userId);
    
    Flux<UserRecommendation> findByUserIdAndCategory(String userId, String category);
    
    Flux<UserRecommendation> findByUserIdAndClickedTrueOrderByClickedAtDesc(String userId);
    
    Flux<UserRecommendation> findByUserIdAndPurchasedTrueOrderByPurchasedAtDesc(String userId);
    
    Flux<UserRecommendation> findByProductIdAndUserIdNot(String productId, String userId);
    
    Flux<UserRecommendation> findByRecommendationTypeOrderByRecommendationScoreDesc(
            RecommendationType type);
    
    Mono<Long> countByUserIdAndClickedTrue(String userId);
    
    Mono<Long> countByUserIdAndPurchasedTrue(String userId);
    
    Mono<Void> deleteByUserIdAndExpiresAtBefore(String userId, LocalDateTime now);
    
    Mono<Void> deleteByExpiresAtBefore(LocalDateTime now);
    
    Flux<UserRecommendation> findByUserIdAndRecommendationScoreGreaterThanOrderByRecommendationScoreDesc(
            String userId, Double minScore);
    
    Flux<UserRecommendation> findByUserIdIn(List<String> userIds);
    
    Flux<UserRecommendation> findByUserIdAndClickedFalseAndRecommendationTypeOrderByRecommendationScoreDesc(
            String userId, RecommendationType type);
}
