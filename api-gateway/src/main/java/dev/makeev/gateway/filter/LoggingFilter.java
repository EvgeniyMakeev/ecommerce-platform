package dev.makeev.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String correlationId = generateCorrelationId(request);
        logRequest(request, correlationId);
        exchange.getResponse().getHeaders().add("X-Correlation-ID", correlationId);
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTime = System.currentTimeMillis();
            logResponse(exchange, correlationId, endTime - startTime);
        }));
    }

    private void logRequest(ServerHttpRequest request, String correlationId) {
        log.info("Request: {} {} | Correlation-ID: {} | Remote: {} | User-Agent: {} | Timestamp: {}",
                request.getMethod(),
                request.getURI(),
                correlationId,
                request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown",
                request.getHeaders().getFirst("User-Agent"),
                Instant.now()
        );
    }

    private void logResponse(ServerWebExchange exchange, String correlationId, long responseTime) {
        log.info("Response: {} {} | Correlation-ID: {} | Status: {} | Response-Time: {}ms | Timestamp: {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                correlationId,
                exchange.getResponse().getStatusCode(),
                responseTime,
                Instant.now()
        );
    }

    private String generateCorrelationId(ServerHttpRequest request) {
        String existingCorrelationId = request.getHeaders().getFirst("X-Correlation-ID");
        if (existingCorrelationId != null && !existingCorrelationId.isEmpty()) {
            return existingCorrelationId;
        }
        return UUID.randomUUID().toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
