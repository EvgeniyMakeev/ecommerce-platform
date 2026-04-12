package dev.makeev.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "api-gateway");
        response.put("status", "UP");
        response.put("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> getServices() {
        Map<String, Object> response = new HashMap<>();
        
        List<String> services = List.of(
            "product-service",
            "search-service",
            "cart-service",
            "inventory-service",
            "order-service",
            "recommendation-service"
        );
        
        response.put("services", services);
        response.put("serviceCount", services.size());
        response.put("discoveryType", "Docker Compose DNS");
        response.put("gatewayService", "api-gateway");
        response.put("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/routes")
    public Mono<ResponseEntity<Map<String, Object>>> getRoutes() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Route information available at /actuator/gateway/routes");
        response.put("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "api-gateway");
        response.put("version", "1.0.0");
        response.put("description", "API Gateway for E-commerce Platform");
        response.put("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
