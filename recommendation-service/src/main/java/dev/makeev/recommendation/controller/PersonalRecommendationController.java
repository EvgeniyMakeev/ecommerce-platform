package dev.makeev.recommendation.controller;

import dev.makeev.recommendation.dto.ActivityRequest;
import dev.makeev.recommendation.dto.DetailedActivityRequest;
import dev.makeev.recommendation.model.UserActivity;
import dev.makeev.recommendation.model.UserRecommendation;
import dev.makeev.recommendation.service.PersonalRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations/personal")
@RequiredArgsConstructor
@Slf4j
public class PersonalRecommendationController {
    
    private final PersonalRecommendationService personalRecommendationService;

    @GetMapping("/{userId}")
    public Flux<UserRecommendation> getPersonalizedRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("GET /api/recommendations/personal/{} - Getting personalized recommendations", userId);
        return personalRecommendationService.getPersonalizedRecommendations(userId, limit);
    }

    @PostMapping("/activity")
    public Mono<ResponseEntity<UserActivity>> trackUserActivity(@Valid @RequestBody ActivityRequest request) {
        log.info("POST /api/recommendations/personal/activity - Tracking activity: {} for user: {}", 
                request.activityType(), request.userId());
        
        return personalRecommendationService.trackUserActivity(
                request.userId(),
                request.productId(),
                request.productName(),
                request.category(),
                request.activityType(),
                request.sessionId()
        ).map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/activity/detailed")
    public Mono<ResponseEntity<UserActivity>> trackUserActivityDetailed(@Valid @RequestBody DetailedActivityRequest request) {
        log.info("POST /api/recommendations/personal/activity/detailed - Tracking detailed activity: {} for user: {}", 
                request.activityType(), request.userId());
        
        return personalRecommendationService.trackUserActivity(
                request.userId(),
                request.productId(),
                request.productName(),
                request.category(),
                request.activityType(),
                request.sessionId(),
                request.price(),
                request.quantity()
        ).map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{userId}/click/{productId}")
    public Mono<ResponseEntity<UserRecommendation>> markRecommendationAsClicked(
            @PathVariable String userId, @PathVariable String productId) {
        log.info("POST /api/recommendations/personal/{}/click/{} - Marking as clicked", userId, productId);
        
        return personalRecommendationService.markRecommendationAsClicked(userId, productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/purchase/{productId}")
    public Mono<ResponseEntity<UserRecommendation>> markRecommendationAsPurchased(
            @PathVariable String userId, @PathVariable String productId) {
        log.info("POST /api/recommendations/personal/{}/purchase/{} - Marking as purchased", userId, productId);
        
        return personalRecommendationService.markRecommendationAsPurchased(userId, productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/stats")
    public Mono<ResponseEntity<Map<String, Object>>> getRecommendationStats(@PathVariable String userId) {
        log.info("GET /api/recommendations/personal/{}/stats - Getting recommendation stats", userId);
        
        return personalRecommendationService.getRecommendationStats(userId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping("/cleanup")
    public Mono<ResponseEntity<Void>> cleanupExpiredRecommendations() {
        log.info("POST /api/recommendations/personal/cleanup - Cleaning up expired recommendations");
        
        return personalRecommendationService.cleanupExpiredRecommendations()
                .map(v -> ResponseEntity.ok().<Void>build())
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
}
