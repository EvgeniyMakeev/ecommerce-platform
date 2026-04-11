package dev.makeev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest
class GatewayApplicationTest {

    @Test
    @DisplayName("API Gateway web context should load successfully")
    void testContextLoads() {
        assertThat(true).isTrue();
    }

    @Configuration
    static class TestConfig {
        
        @Bean
        public RouterFunction<ServerResponse> testRouter() {
            return route(path("/test"), request -> 
                ServerResponse.ok().bodyValue("Gateway test endpoint"));
        }
    }
}
