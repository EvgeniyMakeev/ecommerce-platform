package dev.makeev.gateway.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CircuitBreakerStateService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public Mono<Map<String, Object>> getCircuitBreakerState(String circuitBreakerName) {
        return Mono.fromCallable(() -> {
                    Optional<CircuitBreaker> circuitBreaker = circuitBreakerRegistry.getAllCircuitBreakers()
                            .stream()
                            .filter(cb -> cb.getName().equals(circuitBreakerName))
                            .findFirst();

                    if (circuitBreaker.isEmpty()) {
                        log.warn("Circuit breaker not found: {}", circuitBreakerName);
                        Map<String, Object> state = new HashMap<>();
                        state.put("name", circuitBreakerName);
                        state.put("state", "NOT_FOUND");
                        state.put("message", "Circuit breaker not configured");
                        return state;
                    }

                    CircuitBreaker cb = circuitBreaker.get();
                    CircuitBreaker.State state = cb.getState();
                    CircuitBreaker.Metrics metrics = cb.getMetrics();
                    CircuitBreakerConfig config = cb.getCircuitBreakerConfig();

                    Map<String, Object> stateInfo = new HashMap<>();
                    stateInfo.put("name", cb.getName());
                    stateInfo.put("state", state.toString());
                    stateInfo.put("failureRate", metrics.getFailureRate());
                    stateInfo.put("failureRateThreshold", config.getFailureRateThreshold());
                    stateInfo.put("slowCallRate", metrics.getSlowCallRate());
                    stateInfo.put("slowCallRateThreshold", config.getSlowCallRateThreshold());
                    stateInfo.put("bufferSize", metrics.getNumberOfBufferedCalls());
                    stateInfo.put("failedCalls", metrics.getNumberOfFailedCalls());
                    stateInfo.put("successCalls", metrics.getNumberOfSuccessfulCalls());
                    stateInfo.put("slowCalls", metrics.getNumberOfSlowCalls());
                    stateInfo.put("slidingWindowSize", config.getSlidingWindowSize());
                    stateInfo.put("minimumNumberOfCalls", config.getMinimumNumberOfCalls());

                    switch (state) {
                        case OPEN -> stateInfo.put("message", "Circuit breaker is OPEN - blocking calls");
                        case HALF_OPEN -> stateInfo.put("message", "Circuit breaker is HALF_OPEN - testing recovery");
                        default -> stateInfo.put("message", "Circuit breaker is CLOSED - normal operation");
                    }

                    return stateInfo;
                })
                .onErrorResume(throwable -> {
                    log.error("Error getting circuit breaker state for: {}", circuitBreakerName, throwable);
                    Map<String, Object> errorState = new HashMap<>();
                    errorState.put("name", circuitBreakerName);
                    errorState.put("state", "ERROR");
                    errorState.put("error", throwable.getMessage());
                    return Mono.just(errorState);
                });
    }
}
