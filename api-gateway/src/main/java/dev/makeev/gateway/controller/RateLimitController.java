package dev.makeev.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rate-limit")
@RequiredArgsConstructor
@Slf4j
public class RateLimitController {

    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitStatus() {
        log.info("GET /api/admin/rate-limit/status - Getting rate limit status");
        
        Map<String, Object> status = new HashMap<>();
        status.put("status", "active");
        status.put("type", "redis-based");
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
        
        return Mono.just(ResponseEntity.ok(status));
    }

    @GetMapping("/headers")
    public Mono<ResponseEntity<Map<String, String>>> getRateLimitHeaders() {
        log.info("GET /api/admin/rate-limit/headers - Getting rate limit headers info");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Rate-Limit-Remaining", "Number of remaining requests in current window");
        headers.put("X-Rate-Limit-Limit", "Maximum requests allowed in window");
        headers.put("X-Rate-Limit-Retry-After", "Seconds to wait before retry (429 responses)");
        
        return Mono.just(ResponseEntity.ok(headers));
    }

    @GetMapping("/test")
    public Mono<ResponseEntity<Map<String, Object>>> testRateLimit(@RequestParam(defaultValue = "ip") String keyType) {
        log.info("GET /api/admin/rate-limit/test - Testing rate limit with key type: {}", keyType);
        
        Map<String, Object> test = new HashMap<>();
        test.put("message", "Rate limit test endpoint");
        test.put("keyType", keyType);
        test.put("timestamp", System.currentTimeMillis());
        test.put("note", "Make multiple requests to see rate limiting in action");
        
        return Mono.just(ResponseEntity.ok(test));
    }

    @PostMapping("/reset")
    public Mono<ResponseEntity<Map<String, String>>> resetRateLimit(@RequestBody Map<String, String> request) {
        String keyType = request.getOrDefault("keyType", "ip");
        String key = request.get("key");
        
        log.info("POST /api/admin/rate-limit/reset - Resetting rate limit for key: {} ({})", key, keyType);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "reset_requested");
        response.put("key", key);
        response.put("keyType", keyType);
        response.put("message", "Rate limit reset requested (cache clearing not implemented)");
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
