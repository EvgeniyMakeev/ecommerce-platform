package dev.makeev.recommendation.repository;

import dev.makeev.recommendation.model.ActivityType;
import dev.makeev.recommendation.model.UserActivity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface UserActivityRepository extends R2dbcRepository<UserActivity, Long> {

    Flux<UserActivity> findByUserId(String userId);

    Flux<UserActivity> findByUserIdAndActivityType(String userId, ActivityType activityType);

    Flux<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(String userId, ActivityType activityType);

    Flux<UserActivity> findByProductId(String productId);

    Mono<Long> countByUserIdAndActivityType(String userId, ActivityType activityType);

    Flux<UserActivity> findByUserIdAndActivityTypeInOrderByCreatedAtDesc(String userId, List<ActivityType> activityTypes);
}
