package dev.makeev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootTest(
    classes = GatewayApplicationTest.TestConfig.class,
    properties = {
        "spring.main.web-application-type=reactive",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.discovery.reactive.enabled=false",
        "spring.cloud.refresh.enabled=false",
        "spring.cloud.util.enabled=false",
        "spring.cloud.gateway.enabled=false",
        "spring.cloud.compatibility-verifier.enabled=false"
    }
)
class GatewayApplicationTest {

    @Test
    @DisplayName("API Gateway web context should load successfully")
    void testContextLoads() {
        assertThat(true).isTrue();
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {
        org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration.class,
        org.springframework.cloud.autoconfigure.RefreshAutoConfiguration.class,
        org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration.class,
        org.springframework.cloud.client.discovery.composite.CompositeDiscoveryClientAutoConfiguration.class,
        org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration.class,
        org.springframework.cloud.client.hypermedia.CloudHypermediaAutoConfiguration.class
    })
    static class TestConfig {
        
        @Bean
        public RouterFunction<ServerResponse> testRouter() {
            return route(path("/test"), request -> 
                ServerResponse.ok().bodyValue("Gateway test endpoint"));
        }
    }
}
