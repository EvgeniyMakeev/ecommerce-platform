package dev.makeev.recommendation.service;

import dev.makeev.recommendation.model.ActivityType;
import dev.makeev.recommendation.model.RecommendationType;
import dev.makeev.recommendation.model.UserActivity;
import dev.makeev.recommendation.model.UserRecommendation;
import dev.makeev.recommendation.repository.UserActivityRepository;
import dev.makeev.recommendation.repository.UserRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalRecommendationService {
    
    private final UserActivityRepository userActivityRepository;
    private final UserRecommendationRepository userRecommendationRepository;
    private final PopularProductService popularProductService;

    public Flux<UserRecommendation> getPersonalizedRecommendations(String userId, int limit) {
        log.info("Getting personalized recommendations for user: {}", userId);
        
        return Flux.merge(
                getCollaborativeRecommendations(userId, limit / 4),
                getContentBasedRecommendations(userId, limit / 4),
                getCategoryBasedRecommendations(userId, limit / 4),
                getCrossSellRecommendations(userId, limit / 4)
        )
        .distinct(UserRecommendation::getProductId)
        .sort((r1, r2) -> r2.getRecommendationScore().compareTo(r1.getRecommendationScore()))
        .take(limit)
        .doOnNext(rec -> log.debug("Generated recommendation: {} for user: {}", 
                rec.getProductName(), userId));
    }

    private Flux<UserRecommendation> getCollaborativeRecommendations(String userId, int limit) {
        log.debug("Getting collaborative recommendations for user: {}", userId);
        
        return userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                userId, ActivityType.PURCHASE)
                .take(20) // Get recent purchases
                .flatMap(activity -> 
                        userActivityRepository.findByProductId(activity.getProductId())
                                .filter(otherActivity -> !otherActivity.getUserId().equals(userId))
                                .map(UserActivity::getUserId)
                                .distinct()
                                .take(10) // Similar users
                )
                .distinct()
                .flatMap(similarUserId -> 
                        userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                                similarUserId, ActivityType.PURCHASE)
                                .take(10) // Their purchases
                                .filter(activity -> !userAlreadyPurchased(userId, activity.getProductId()))
                )
                .map(activity -> UserRecommendation.builder()
                        .userId(userId)
                        .productId(activity.getProductId())
                        .productName(activity.getProductName())
                        .category(activity.getCategory())
                        .price(activity.getPrice())
                        .imageUrl(null) // imageUrl would be fetched from product service
                        .recommendationType(RecommendationType.COLLABORATIVE_FILTERING)
                        .recommendationScore(calculateCollaborativeScore(userId, activity.getProductId()))
                        .reason("Users who bought this also liked")
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build())
                .take(limit);
    }

    private Flux<UserRecommendation> getContentBasedRecommendations(String userId, int limit) {
        log.debug("Getting content-based recommendations for user: {}", userId);
        
        return userActivityRepository.findByUserIdAndActivityTypeInOrderByCreatedAtDesc(
                userId, Arrays.asList(
                        ActivityType.VIEW,
                        ActivityType.PURCHASE,
                        ActivityType.ADD_TO_CART
                ))
                .take(50) // Recent activity
                .collectList()
                .flatMapMany(activities -> {
                    Map<String, Integer> categoryPreferences = new HashMap<>();
                    Map<String, Double> pricePreferences = new HashMap<>();
                    
                    activities.forEach(activity -> {
                        categoryPreferences.merge(activity.getCategory(), 1, Integer::sum);
                        if (activity.getPrice() != null) {
                            pricePreferences.merge(activity.getCategory(), activity.getPrice(), Double::sum);
                        }
                    });
                    
                    List<String> preferredCategories = categoryPreferences.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    
                    return Flux.fromIterable(preferredCategories)
                            .flatMap(category -> 
                                    popularProductService.getTopPopularProductsByCategory(category)
                                            .take(limit / preferredCategories.size())
                            )
                            .filter(product -> !userAlreadyInteracted(userId, product.getProductId()))
                            .map(product -> {
                                double score = categoryPreferences.getOrDefault(product.getCategory(), 0) * 0.1;
                                return UserRecommendation.builder()
                                        .userId(userId)
                                        .productId(product.getProductId())
                                        .productName(product.getProductName())
                                        .category(product.getCategory())
                                        .price(product.getPrice().doubleValue())
                                        .imageUrl(product.getImageUrl())
                                        .recommendationType(RecommendationType.CONTENT_BASED)
                                        .recommendationScore(score)
                                        .reason("Based on your interests in " + product.getCategory())
                                        .createdAt(LocalDateTime.now())
                                        .expiresAt(LocalDateTime.now().plusDays(7))
                                        .build();
                            });
                })
                .take(limit);
    }

    private Flux<UserRecommendation> getCategoryBasedRecommendations(String userId, int limit) {
        log.debug("Getting category-based recommendations for user: {}", userId);
        
        return userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                userId, ActivityType.VIEW)
                .take(30) // Recent views
                .groupBy(UserActivity::getCategory)
                .flatMap(group -> 
                        group.key().equals("uncategorized") ? 
                                Flux.empty() : 
                                group.count().map(count -> Map.entry(group.key(), count))
                )
                .sort(Map.Entry.<String, Long>comparingByValue().reversed())
                .take(3) // Top 3 categories
                .flatMap(entry -> 
                        popularProductService.getTopPopularProductsByCategory(entry.getKey())
                                .take(limit / 3)
                )
                .filter(product -> !userAlreadyInteracted(userId, product.getProductId()))
                .map(product -> UserRecommendation.builder()
                        .userId(userId)
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .category(product.getCategory())
                        .price(product.getPrice().doubleValue())
                        .imageUrl(product.getImageUrl())
                        .recommendationType(RecommendationType.CATEGORY_BASED)
                        .recommendationScore(product.getPopularityScore())
                        .reason("Popular in " + product.getCategory())
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build())
                .take(limit);
    }

    private Flux<UserRecommendation> getCrossSellRecommendations(String userId, int limit) {
        log.debug("Getting cross-sell recommendations for user: {}", userId);
        
        return userActivityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(
                userId, ActivityType.PURCHASE)
                .take(10) // Recent purchases
                .flatMap(purchase -> 
                        userActivityRepository.findByProductId(purchase.getProductId())
                                .filter(activity -> activity.getActivityType() == ActivityType.PURCHASE)
                                .filter(activity -> !activity.getUserId().equals(userId))
                                .map(UserActivity::getProductId)
                                .distinct()
                                .take(5) // Frequently bought together
                )
                .distinct()
                .flatMap(relatedProductId -> 
                        popularProductService.getAllPopularProducts()
                                .filter(product -> product.getProductId().equals(relatedProductId))
                                .take(1)
                )
                .filter(product -> !userAlreadyInteracted(userId, product.getProductId()))
                .map(product -> UserRecommendation.builder()
                        .userId(userId)
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .category(product.getCategory())
                        .price(product.getPrice().doubleValue())
                        .imageUrl(product.getImageUrl())
                        .recommendationType(RecommendationType.CROSS_SELL)
                        .recommendationScore(product.getPopularityScore())
                        .reason("Frequently bought together with your items")
                        .createdAt(LocalDateTime.now())
                        .expiresAt(LocalDateTime.now().plusDays(7))
                        .build())
                .take(limit);
    }

    public Mono<UserActivity> trackUserActivity(String userId, String productId, String productName,
                                              String category, ActivityType activityType,
                                              String sessionId) {
        log.info("Tracking activity: {} for user: {} on product: {}", activityType, userId, productId);
        
        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .category(category)
                .activityType(activityType)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
        
        return userActivityRepository.save(activity)
                .doOnSuccess(savedActivity -> {
                    log.debug("Successfully tracked activity: {} for user: {}", 
                            savedActivity.getActivityType(), savedActivity.getUserId());
                    
                    // Trigger recommendation refresh for certain activities
                    if (activityType == ActivityType.PURCHASE || 
                        activityType == ActivityType.ADD_TO_CART) {
                        refreshRecommendations(userId);
                    }
                });
    }

    public Mono<UserActivity> trackUserActivity(String userId, String productId, String productName,
                                              String category, ActivityType activityType,
                                              String sessionId, Double price, Integer quantity) {
        log.info("Tracking activity: {} for user: {} on product: {} with price: {}", 
                activityType, userId, productId, price);
        
        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .category(category)
                .activityType(activityType)
                .sessionId(sessionId)
                .price(price)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build();
        
        return userActivityRepository.save(activity)
                .doOnSuccess(savedActivity -> 
                        log.debug("Successfully tracked activity with data: {} for user: {}", 
                                savedActivity.getActivityType(), savedActivity.getUserId()));
    }

    public Mono<UserRecommendation> markRecommendationAsClicked(String userId, String productId) {
        log.info("Marking recommendation as clicked: {} for user: {}", productId, userId);
        
        return userRecommendationRepository.findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(
                        userId, LocalDateTime.now())
                .filter(rec -> rec.getProductId().equals(productId))
                .next()
                .flatMap(rec -> {
                    rec.markAsClicked();
                    return userRecommendationRepository.save(rec);
                })
                .doOnSuccess(rec -> log.debug("Marked recommendation as clicked: {} for user: {}", 
                        rec.getProductId(), rec.getUserId()));
    }

    public Mono<UserRecommendation> markRecommendationAsPurchased(String userId, String productId) {
        log.info("Marking recommendation as purchased: {} for user: {}", productId, userId);
        
        return userRecommendationRepository.findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(
                        userId, LocalDateTime.now())
                .filter(rec -> rec.getProductId().equals(productId))
                .next()
                .flatMap(rec -> {
                    rec.markAsPurchased();
                    return userRecommendationRepository.save(rec);
                })
                .doOnSuccess(rec -> log.debug("Marked recommendation as purchased: {} for user: {}", 
                        rec.getProductId(), rec.getUserId()));
    }

    public Mono<Void> cleanupExpiredRecommendations() {
        log.info("Cleaning up expired recommendations");
        
        return userRecommendationRepository.deleteByExpiresAtBefore(LocalDateTime.now())
                .doOnSuccess(unused -> log.debug("Cleaned up expired recommendations"));
    }

    public Mono<Map<String, Object>> getRecommendationStats(String userId) {
        log.info("Getting recommendation stats for user: {}", userId);
        
        return Mono.zip(
                userRecommendationRepository.countByUserIdAndClickedTrue(userId),
                userRecommendationRepository.countByUserIdAndPurchasedTrue(userId),
                userActivityRepository.countByUserIdAndActivityType(userId, ActivityType.VIEW)
        )
        .map(tuple -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("userId", userId);
            stats.put("totalRecommendations", tuple.getT1() + tuple.getT2());
            stats.put("clickedRecommendations", tuple.getT1());
            stats.put("purchasedRecommendations", tuple.getT2());
            stats.put("clickThroughRate", tuple.getT1() > 0 ? 
                    (double) tuple.getT2() / tuple.getT1() : 0.0);
            stats.put("totalViews", tuple.getT3());
            return stats;
        });
    }
    
    // Helper methods
    private boolean userAlreadyPurchased(String userId, String productId) {
        return userActivityRepository.findByUserIdAndActivityType(userId, ActivityType.PURCHASE)
                .any(activity -> activity.getProductId().equals(productId))
                .block();
    }
    
    private boolean userAlreadyInteracted(String userId, String productId) {
        return userActivityRepository.findByUserId(userId)
                .any(activity -> activity.getProductId().equals(productId))
                .block();
    }
    
    private double calculateCollaborativeScore(String userId, String productId) {
        return userActivityRepository.findByProductId(productId)
                .count()
                .map(count -> Math.log1p(count) * 0.5)
                .block();
    }
    
    private void refreshRecommendations(String userId) {
        log.debug("Triggering recommendation refresh for user: {}", userId);
    }
}
