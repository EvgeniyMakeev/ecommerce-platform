package dev.makeev.gateway.controller;

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
public class FallbackController {

    @GetMapping("/product")
    public Mono<ResponseEntity<Map<String, Object>>> productFallback() {
        log.warn("Product service fallback activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "product-service");
        response.put("status", "unavailable");
        response.put("message", "Product service is currently unavailable. Please try again later.");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchFallback() {
        log.warn("Search service fallback activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "search-service");
        response.put("status", "unavailable");
        response.put("message", "Search service is currently unavailable. Please try again later.");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/cart")
    public Mono<ResponseEntity<Map<String, Object>>> cartFallback() {
        log.warn("Cart service fallback activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "cart-service");
        response.put("status", "unavailable");
        response.put("message", "Cart service is currently unavailable. Please try again later.");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/inventory")
    public Mono<ResponseEntity<Map<String, Object>>> inventoryFallback() {
        log.warn("Inventory service fallback activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "inventory-service");
        response.put("status", "unavailable");
        response.put("message", "Inventory service is currently unavailable. Please try again later.");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/order")
    public Mono<ResponseEntity<Map<String, Object>>> orderFallback() {
        log.warn("Order service fallback activated");
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", "order-service");
        response.put("status", "unavailable");
        response.put("message", "Order service is currently unavailable. Please try again later.");
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/service/{serviceName}")
    public Mono<ResponseEntity<Map<String, Object>>> genericFallback(@PathVariable String serviceName) {
        log.warn("Generic fallback activated for service: {}", serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("service", serviceName);
        response.put("status", "unavailable");
        response.put("message", String.format("Service %s is currently unavailable. Please try again later.", serviceName));
        response.put("timestamp", Instant.now());
        response.put("fallback", true);
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
