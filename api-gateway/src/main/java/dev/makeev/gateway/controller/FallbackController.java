package dev.makeev.gateway.controller;

import dev.makeev.gateway.service.CircuitBreakerStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
@RequiredArgsConstructor
public class FallbackController {

    private final CircuitBreakerStateService circuitBreakerStateService;

    @GetMapping("/product")
    public Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        log.warn("Product service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("productCircuitBreaker")
                .map(state -> buildFallbackResponse("product-service", state));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        log.warn("Search service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("searchCircuitBreaker")
                .map(state -> buildFallbackResponse("search-service", state));
    }

    @GetMapping("/cart")
    public Mono<ResponseEntity<Map<String, Object>>> cartFallback() {
        log.warn("Cart service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("cartCircuitBreaker")
                .map(state -> buildFallbackResponse("cart-service", state));
    }

    @GetMapping("/inventory")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryFallback() {
        log.warn("Inventory service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("inventoryCircuitBreaker")
                .map(state -> buildFallbackResponse("inventory-service", state));
    }

    @GetMapping("/order")
    public Mono<ResponseEntity<Map<String, Object>>> orderFallback() {
        log.warn("Order service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("orderCircuitBreaker")
                .map(state -> buildFallbackResponse("order-service", state));
    }

    @GetMapping("/recommendation")
    public Mono<ResponseEntity<Map<String, Object>>> recommendationFallback() {
        log.warn("Recommendation service fallback activated");
        
        return circuitBreakerStateService.getCircuitBreakerState("recommendationCircuitBreaker")
                .map(state -> buildFallbackResponse("recommendation-service", state));
    }

    @GetMapping("/service/{serviceName}")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback(@PathVariable String serviceName) {
        log.warn("Generic fallback activated for service: {}", serviceName);
        
        String circuitBreakerName = serviceName + "CircuitBreaker";
        return circuitBreakerStateService.getCircuitBreakerState(circuitBreakerName)
                .map(state -> buildFallbackResponse(serviceName, state))
                .onErrorResume(throwable -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("service", serviceName);
                    response.put("status", "unavailable");
                    response.put("message", String.format("Service %s is currently unavailable. Please try again later.", serviceName));
                    response.put("timestamp", Instant.now());
                    response.put("fallback", true);
                    response.put("circuitBreaker", "unknown");
                    return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
                });
    }

    private ResponseEntity<Map<String, Object>> buildFallbackResponse(String serviceName, Map<String, Object> circuitBreakerState) {
        Map<String, Object> response = new HashMap<>();
        response.put("service", serviceName);
        response.put("status", "unavailable");
        response.put("message", String.format("%s is currently unavailable due to circuit breaker being %s. Please try again later.", 
                serviceName, circuitBreakerState.get("state").toString().toLowerCase()));
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        response.put("circuitBreaker", circuitBreakerState);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
