package dev.makeev.gateway.controller;

import dev.makeev.gateway.service.RateLimitService;
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

    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitStatus() {
        log.info("GET /api/admin/rate-limit/status - Getting rate limit status");
        
        return rateLimitService.getRateLimitStatus()
                .map(ResponseEntity::ok);
    }

    @GetMapping("/details")
    public Mono<ResponseEntity<Map<String, Object>>> getRateLimitDetails(
            @RequestParam String key,
            @RequestParam(defaultValue = "ip") String keyType) {
        log.info("GET /api/admin/rate-limit/details - Getting rate limit details for key: {} ({})", key, keyType);
        
        String fullKey = keyType + ":" + key;
        return rateLimitService.getRateLimitDetails(fullKey)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<Map<String, Object>>> getAllRateLimitDetails() {
        log.info("GET /api/admin/rate-limit/all - Getting all rate limit details");
        
        return rateLimitService.getAllRateLimitDetails()
                .map(ResponseEntity::ok);
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
        
        if (key == null || key.isEmpty()) {
            log.warn("POST /api/admin/rate-limit/reset - Missing key parameter");
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Key parameter is required");
            return Mono.just(ResponseEntity.badRequest().body(error));
        }
        
        log.info("POST /api/admin/rate-limit/reset - Resetting rate limit for key: {} ({})", key, keyType);
        
        return rateLimitService.resetRateLimit(key, keyType)
                .map(ResponseEntity::ok);
    }
}
