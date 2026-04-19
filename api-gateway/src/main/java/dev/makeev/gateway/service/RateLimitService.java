package dev.makeev.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    public Mono<Map<String, Object>> getRateLimitStatus() {
        return redisTemplate.keys(RATE_LIMIT_PREFIX + "*")
                .collectList()
                .map(keys -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", "active");
                    status.put("type", "redis-based");
                    status.put("activeKeys", keys.size());
                    status.put("keys", keys);
                    status.put("configurations", Map.of(
                            "search-service", "100 requests per minute",
                            "cart-service", "100 requests per minute",
                            "inventory-service", "100 requests per minute",
                            "order-service", "20 requests per minute (strict)",
                            "recommendation-service", "1000 requests per minute (permissive)"
                    ));
                    status.put("keyTypes", Map.of(
                            "ip", "Based on client IP address",
                            "user", "Based on X-User-ID header",
                            "session", "Based on X-Session-ID header"
                    ));
                    return status;
                })
                .onErrorResume(throwable -> {
                    log.error("Error getting rate limit status", throwable);
                    Map<String, Object> errorStatus = new HashMap<>();
                    errorStatus.put("status", "error");
                    errorStatus.put("error", throwable.getMessage());
                    return Mono.just(errorStatus);
                });
    }

    public Mono<Map<String, Object>> getRateLimitDetails(String key) {
        String redisKey = RATE_LIMIT_PREFIX + key;
        return redisTemplate.opsForValue().get(redisKey)
                .zipWith(redisTemplate.getExpire(redisKey))
                .map(tuple -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("key", key);
                    details.put("count", Integer.parseInt(tuple.getT1()));
                    details.put("ttlSeconds", tuple.getT2());
                    details.put("redisKey", redisKey);
                    return details;
                })
                .switchIfEmpty(Mono.just(Map.of(
                        "key", key,
                        "count", 0,
                        "ttlSeconds", -1,
                        "redisKey", redisKey,
                        "message", "No active rate limit for this key"
                )))
                .onErrorResume(throwable -> {
                    log.error("Error getting rate limit details for key: {}", key, throwable);
                    return Mono.just(Map.of(
                            "key", key,
                            "error", throwable.getMessage()
                    ));
                });
    }

    public Mono<Map<String, String>> resetRateLimit(String key, String keyType) {
        String fullKey = keyType + ":" + key;
        String redisKey = RATE_LIMIT_PREFIX + fullKey;

        return redisTemplate.delete(redisKey)
                .map(deleted -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", deleted > 0 ? "reset" : "not_found");
                    response.put("key", key);
                    response.put("keyType", keyType);
                    response.put("fullKey", fullKey);
                    response.put("redisKey", redisKey);
                    response.put("message", deleted > 0
                            ? "Rate limit successfully reset for key: " + fullKey
                            : "No rate limit found for key: " + fullKey);
                    return response;
                })
                .onErrorResume(throwable -> {
                    log.error("Error resetting rate limit for key: {}", fullKey, throwable);
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("key", key);
                    errorResponse.put("keyType", keyType);
                    errorResponse.put("error", throwable.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    public Mono<Map<String, Object>> getAllRateLimitDetails() {
        return redisTemplate.keys(RATE_LIMIT_PREFIX + "*")
                .flatMap(key ->
                        redisTemplate.opsForValue().get(key)
                                .zipWith(redisTemplate.getExpire(key))
                                .map(tuple -> {
                                    Map<String, Object> details = new HashMap<>();
                                    details.put("redisKey", key);
                                    details.put("count", Integer.parseInt(tuple.getT1()));
                                    details.put("ttlSeconds", tuple.getT2());
                                    return details;
                                })
                )
                .collectList()
                .map(details -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("totalKeys", details.size());
                    result.put("details", details);
                    return result;
                })
                .onErrorResume(throwable -> {
                    log.error("Error getting all rate limit details", throwable);
                    return Mono.just(Map.of(
                            "error", throwable.getMessage()
                    ));
                });
    }
}
