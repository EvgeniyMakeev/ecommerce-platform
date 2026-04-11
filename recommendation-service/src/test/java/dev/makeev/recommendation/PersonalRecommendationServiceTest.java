package dev.makeev.recommendation;

import dev.makeev.recommendation.model.ActivityType;
import dev.makeev.recommendation.model.RecommendationType;
import dev.makeev.recommendation.model.UserActivity;
import dev.makeev.recommendation.model.UserRecommendation;
import dev.makeev.recommendation.repository.UserActivityRepository;
import dev.makeev.recommendation.repository.UserRecommendationRepository;
import dev.makeev.recommendation.service.PersonalRecommendationService;
import dev.makeev.recommendation.service.PopularProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalRecommendationService Tests")
class PersonalRecommendationServiceTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private UserRecommendationRepository userRecommendationRepository;

    @Mock
    private PopularProductService popularProductService;

    @InjectMocks
    private PersonalRecommendationService personalRecommendationService;

    @Test
    @DisplayName("Should get personalized recommendations")
    void testGetPersonalizedRecommendations() {
        String userId = "user123";
        
        when(userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                anyString(), any(ActivityType.class)))
                .thenReturn(Flux.empty());
        
        when(userActivityRepository.findByUserIdAndActivityTypeInOrderByCreatedAtDesc(
                anyString(), any()))
                .thenReturn(Flux.empty());

        StepVerifier.create(personalRecommendationService.getPersonalizedRecommendations(userId, 10))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should track user activity")
    void testTrackUserActivity() {
        String userId = "user123";
        String productId = "prod-001";
        String productName = "iPhone 15";
        String category = "Electronics";
        ActivityType activityType = ActivityType.VIEW;
        String sessionId = "session1";
        
        UserActivity expectedActivity = UserActivity.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .category(category)
                .activityType(activityType)
                .sessionId(sessionId)
                .build();
        
        when(userActivityRepository.save(any(UserActivity.class)))
                .thenReturn(Mono.just(expectedActivity));

        StepVerifier.create(personalRecommendationService.trackUserActivity(
                userId, productId, productName, category, activityType, sessionId))
                .expectNext(expectedActivity)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should track user activity with additional data")
    void testTrackUserActivityDetailed() {
        String userId = "user123";
        String productId = "prod-001";
        String productName = "iPhone 15";
        String category = "Electronics";
        ActivityType activityType = ActivityType.PURCHASE;
        String sessionId = "session1";
        Double price = 1199.99;
        Integer quantity = 1;
        
        UserActivity expectedActivity = UserActivity.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .category(category)
                .activityType(activityType)
                .sessionId(sessionId)
                .price(price)
                .quantity(quantity)
                .build();
        
        when(userActivityRepository.save(any(UserActivity.class)))
                .thenReturn(Mono.just(expectedActivity));

        StepVerifier.create(personalRecommendationService.trackUserActivity(
                userId, productId, productName, category, activityType, sessionId, price, quantity))
                .expectNext(expectedActivity)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should mark recommendation as clicked")
    void testMarkRecommendationAsClicked() {
        String userId = "user123";
        String productId = "prod-001";
        
        UserRecommendation recommendation = UserRecommendation.builder()
                .userId(userId)
                .productId(productId)
                .productName("iPhone 15")
                .category("Electronics")
                .price(1199.99)
                .imageUrl(null)
                .recommendationType(RecommendationType.CONTENT_BASED)
                .recommendationScore(0.8)
                .reason("Based on your interests")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        when(userRecommendationRepository.findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(
                eq(userId), any(LocalDateTime.class)))
                .thenReturn(Flux.just(recommendation));
        
        when(userRecommendationRepository.save(any(UserRecommendation.class)))
                .thenReturn(Mono.just(recommendation));

        StepVerifier.create(personalRecommendationService.markRecommendationAsClicked(userId, productId))
                .expectNext(recommendation)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should mark recommendation as purchased")
    void testMarkRecommendationAsPurchased() {
        String userId = "user123";
        String productId = "prod-001";
        
        UserRecommendation recommendation = UserRecommendation.builder()
                .userId(userId)
                .productId(productId)
                .productName("iPhone 15")
                .category("Electronics")
                .price(1199.99)
                .imageUrl(null)
                .recommendationType(RecommendationType.CONTENT_BASED)
                .recommendationScore(0.8)
                .reason("Based on your interests")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        when(userRecommendationRepository.findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(
                eq(userId), any(LocalDateTime.class)))
                .thenReturn(Flux.just(recommendation));
        
        when(userRecommendationRepository.save(any(UserRecommendation.class)))
                .thenReturn(Mono.just(recommendation));

        StepVerifier.create(personalRecommendationService.markRecommendationAsPurchased(userId, productId))
                .expectNext(recommendation)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should cleanup expired recommendations")
    void testCleanupExpiredRecommendations() {
        when(userRecommendationRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(personalRecommendationService.cleanupExpiredRecommendations())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get recommendation statistics")
    void testGetRecommendationStats() {
        String userId = "user123";
        
        when(userRecommendationRepository.countByUserIdAndClickedTrue(userId))
                .thenReturn(Mono.just(10L));
        
        when(userRecommendationRepository.countByUserIdAndPurchasedTrue(userId))
                .thenReturn(Mono.just(5L));
        
        when(userActivityRepository.countByUserIdAndActivityType(userId, ActivityType.VIEW))
                .thenReturn(Mono.just(100L));

        StepVerifier.create(personalRecommendationService.getRecommendationStats(userId))
                .expectNextMatches(stats -> stats.get("userId").equals(userId) &&
                       stats.get("clickedRecommendations").equals(10L) &&
                       stats.get("purchasedRecommendations").equals(5L) &&
                       stats.get("totalViews").equals(100L))
                .verifyComplete();
    }
}
