package dev.makeev.gateway.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;
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
@RequiredArgsConstructor
public class GatewayController {

    private final RouteLocator routeLocator;
    private final Environment environment;

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "api-gateway");
        response.put("status", "UP");
        response.put("timestamp", Instant.now());
        response.put("profiles", List.of(environment.getActiveProfiles()));
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/services")
    public Mono<ResponseEntity<Map<String, Object>>> getServices() {
        List<String> services = List.of(
            "product-service",
            "search-service",
            "cart-service",
            "inventory-service",
            "order-service",
            "recommendation-service"
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("services", services);
        response.put("serviceCount", services.size());
        response.put("discoveryType", "Docker Compose DNS");
        response.put("gatewayService", "api-gateway");
        response.put("timestamp", Instant.now());
        response.put("activeProfiles", List.of(environment.getActiveProfiles()));
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/routes")
    public Mono<ResponseEntity<Map<String, Object>>> getRoutes() {
        List<Map<String, Object>> routes = routeLocator.getRoutes()
                .map(route -> {
                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("id", route.getId());
                    routeInfo.put("uri", route.getUri().toString());
                    return routeInfo;
                })
                .collectList()
                .block();

        Map<String, Object> response = new HashMap<>();
        response.put("routes", routes);
        response.put("routeCount", routes.size());
        response.put("timestamp", Instant.now());

        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> getInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "api-gateway");
        response.put("version", environment.getProperty("app.version", "1.0.0"));
        response.put("description", "API Gateway for E-commerce Platform");
        response.put("port", environment.getProperty("server.port", "8080"));
        response.put("profiles", List.of(environment.getActiveProfiles()));
        response.put("timestamp", Instant.now());
        
        return Mono.just(ResponseEntity.ok(response));
    }
}
