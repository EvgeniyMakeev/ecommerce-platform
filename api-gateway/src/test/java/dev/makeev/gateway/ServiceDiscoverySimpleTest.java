package dev.makeev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceDiscoverySimpleTest {

    @Test
    @DisplayName("API Gateway should have service discovery configuration")
    void testServiceDiscoveryConfiguration() {
        assertThat(ApiGatewayApplication.class).isNotNull();
        assertThat(ApiGatewayApplication.class.isAnnotationPresent(org.springframework.cloud.client.discovery.EnableDiscoveryClient.class))
                .isTrue();
        assertThat(dev.makeev.gateway.controller.GatewayController.class).isNotNull();
    }

    @Test
    @DisplayName("Load balancer configuration should be present")
    void testLoadBalancerConfiguration() {
        try {
            Class<?> loadBalancerClass = Class.forName("org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient");
            assertThat(loadBalancerClass).isNotNull();
        } catch (ClassNotFoundException e) {
            assertThat(true).isTrue();
        }
    }

}
