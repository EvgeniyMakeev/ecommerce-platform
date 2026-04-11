package dev.makeev.recommendation.repository;

import dev.makeev.recommendation.model.ActivityType;
import dev.makeev.recommendation.model.UserActivity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityRepository extends R2dbcRepository<UserActivity, Long> {
    
    Flux<UserActivity> findByUserId(String userId);
    
    Flux<UserActivity> findByUserIdAndActivityType(String userId, 
                                                   ActivityType activityType);
    
    Flux<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(String userId, 
                                                                   ActivityType activityType);
    
    Flux<UserActivity> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Flux<UserActivity> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);
    
    Flux<UserActivity> findByProductId(String productId);
    
    Flux<UserActivity> findByCategory(String category);
    
    Flux<UserActivity> findByUserIdAndCategory(String userId, String category);
    
    Flux<UserActivity> findByUserIdAndActivityTypeAndCreatedAtAfter(String userId, 
                                                                   ActivityType activityType, 
                                                                   LocalDateTime after);
    
    Flux<UserActivity> findTop50ByUserIdOrderByCreatedAtDesc(String userId);
    
    Flux<UserActivity> findBySessionId(String sessionId);
    
    Flux<UserActivity> findByUserIdAndSessionId(String userId, String sessionId);
    
    Mono<Long> countByUserIdAndActivityType(String userId, ActivityType activityType);
    
    Mono<Long> countByUserIdAndActivityTypeAndCreatedAtAfter(String userId, 
                                                             ActivityType activityType, 
                                                             LocalDateTime after);
    
    Flux<UserActivity> findByUserIdAndActivityTypeInOrderByCreatedAtDesc(String userId, 
                                                                      List<ActivityType> activityTypes);
    
    Flux<UserActivity> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, 
                                                           LocalDateTime end);
    
    Flux<UserActivity> findByCategoryAndActivityTypeOrderByCreatedAtDesc(String category, 
                                                                   ActivityType activityType);
    
    Flux<UserActivity> findByUserIdAndActivityTypeAndCategoryOrderByCreatedAtDesc(String userId, 
                                                                              ActivityType activityType, 
                                                                              String category);
}
