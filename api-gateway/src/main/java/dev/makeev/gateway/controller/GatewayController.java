package dev.makeev.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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

    private DiscoveryClient discoveryClient;

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
        
        try {
            List<String> services = discoveryClient.getServices();
            response.put("services", services);
            response.put("serviceCount", services.size());
            
            Map<String, List<ServiceInstance>> instances = new HashMap<>();
            Map<String, Integer> instanceCounts = new HashMap<>();
            
            for (String service : services) {
                List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
                instances.put(service, serviceInstances);
                instanceCounts.put(service, serviceInstances.size());
            }
            response.put("instances", instances);
            response.put("instanceCounts", instanceCounts);
            
            response.put("discoveryType", "Consul");
            response.put("gatewayService", "api-gateway");
            
        } catch (Exception e) {
            log.error("Error getting services", e);
            response.put("error", "Failed to retrieve services");
            response.put("errorDetails", e.getMessage());
        }
        
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
