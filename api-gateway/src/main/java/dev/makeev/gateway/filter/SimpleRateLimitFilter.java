package dev.makeev.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
public class SimpleRateLimitFilter extends AbstractGatewayFilterFactory<SimpleRateLimitFilterConfig> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public SimpleRateLimitFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(SimpleRateLimitFilterConfig.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(SimpleRateLimitFilterConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            String key = generateKey(request, config.getKeyType());
            String redisKey = "rate_limit:" + key;
            
            return redisTemplate.opsForValue()
                    .increment(redisKey)
                    .flatMap(count -> {
                        if (count == 1) {
                            return redisTemplate.expire(redisKey, Duration.ofSeconds(config.getWindowSeconds()))
                                    .then(Mono.just(count));
                        }
                        return Mono.just(count);
                    })
                    .flatMap(count -> {
                        log.debug("Rate limit check for key: {}, count: {}, limit: {}", 
                                key, count, config.getLimit());
                        
                        response.getHeaders().add("X-Rate-Limit-Limit", String.valueOf(config.getLimit()));
                        response.getHeaders().add("X-Rate-Limit-Remaining", 
                                String.valueOf(Math.max(0, config.getLimit() - count)));
                        
                        if (count > config.getLimit()) {
                            log.warn("Rate limit exceeded for key: {}, count: {}", key, count);
                            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                            response.getHeaders().add("X-Rate-Limit-Retry-After", 
                                    String.valueOf(config.getWindowSeconds()));
                            return response.setComplete();
                        }
                        
                        return chain.filter(exchange);
                    })
                    .onErrorResume(throwable -> {
                        log.error("Error in rate limiting", throwable);
                        return chain.filter(exchange);
                    });
        };
    }

    private String generateKey(ServerHttpRequest request, String keyType) {
        return switch (keyType.toLowerCase()) {
            case "ip" -> request.getRemoteAddress() != null ? 
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
            case "user" -> {
                String userId = request.getHeaders().getFirst("X-User-ID");
                yield userId != null ? userId : "anonymous";
            }
            case "session" -> {
                String sessionId = request.getHeaders().getFirst("X-Session-ID");
                yield sessionId != null ? sessionId : "no-session";
            }
            default -> "default";
        };
    }
}
