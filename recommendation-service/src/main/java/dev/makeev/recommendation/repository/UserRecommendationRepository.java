package dev.makeev.recommendation.repository;

import dev.makeev.recommendation.model.UserRecommendation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface UserRecommendationRepository extends R2dbcRepository<UserRecommendation, Long> {

    Flux<UserRecommendation> findByUserIdAndClickedFalseAndExpiresAtAfterOrderByRecommendationScoreDesc(String userId, LocalDateTime now);

    Mono<Long> countByUserIdAndClickedTrue(String userId);

    Mono<Long> countByUserIdAndPurchasedTrue(String userId);

    Mono<Void> deleteByExpiresAtBefore(LocalDateTime now);
}
