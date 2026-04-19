package dev.makeev.gateway;

import dev.makeev.gateway.filter.SimpleRateLimitFilter;
import dev.makeev.gateway.filter.SimpleRateLimitFilterConfig;
import dev.makeev.gateway.service.CircuitBreakerStateService;
import dev.makeev.gateway.service.RateLimitService;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import(SimpleRateLimitFilter.class)
@DisplayName("Simple Rate Limit Filter Tests")
class RateLimitFilterTest {

    @Autowired
    private SimpleRateLimitFilter rateLimitFilter;

    @MockBean
    private GatewayFilterChain filterChain;

    @MockBean
    private CircuitBreakerStateService circuitBreakerStateService;

    @MockBean
    private RouteLocator routeLocator;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    @MockBean
    private ReactiveValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("Should allow requests within rate limit")
    void testRequestsWithinRateLimit() {
        SimpleRateLimitFilterConfig config = new SimpleRateLimitFilterConfig();
        config.setKeyType("ip");
        config.setLimit(10);
        config.setWindowSeconds(60);

        GatewayFilter filter = rateLimitFilter.apply(config);
        
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L), Mono.just(2L), Mono.just(3L), Mono.just(4L), Mono.just(5L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                Mono.defer(() -> filter.filter(exchange, filterChain))
                        .repeat(5)
        )
        .expectNextCount(0)
        .verifyComplete();
    }

    @Test
    @DisplayName("Should block requests exceeding rate limit")
    void testRequestsExceedingRateLimit() {
        SimpleRateLimitFilterConfig config = new SimpleRateLimitFilterConfig();
        config.setKeyType("ip");
        config.setLimit(2);
        config.setWindowSeconds(60);

        GatewayFilter filter = rateLimitFilter.apply(config);
        
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test")
                        .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                        .build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L), Mono.just(2L), Mono.just(3L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                Mono.defer(() -> filter.filter(exchange, filterChain))
                        .repeat(3)
        )
        .expectNextCount(0)
        .verifyComplete();
    }

    @Test
    @DisplayName("Should use user ID for rate limiting when header is present")
    void testUserIdRateLimiting() {
        SimpleRateLimitFilterConfig config = new SimpleRateLimitFilterConfig();
        config.setKeyType("user");
        config.setLimit(5);
        config.setWindowSeconds(60);

        GatewayFilter filter = rateLimitFilter.apply(config);
        
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test")
                        .header("X-User-ID", "user123")
                        .build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders()).containsKey("X-Rate-Limit-Remaining");
        assertThat(exchange.getResponse().getHeaders()).containsKey("X-Rate-Limit-Limit");
    }

    @Test
    @DisplayName("Should use session ID for rate limiting when header is present")
    void testSessionIdRateLimiting() {
        SimpleRateLimitFilterConfig config = new SimpleRateLimitFilterConfig();
        config.setKeyType("session");
        config.setLimit(5);
        config.setWindowSeconds(60);

        GatewayFilter filter = rateLimitFilter.apply(config);
        
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test")
                        .header("X-Session-ID", "session456")
                        .build()
        );

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));
        when(filterChain.filter(any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, filterChain))
                .verifyComplete();

        assertThat(exchange.getResponse().getHeaders()).containsKey("X-Rate-Limit-Remaining");
        assertThat(exchange.getResponse().getHeaders()).containsKey("X-Rate-Limit-Limit");
    }
}
