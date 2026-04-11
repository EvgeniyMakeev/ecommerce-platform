package dev.makeev.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayCompilationTest {

    @Test
    @DisplayName("API Gateway classes should compile successfully")
    void testGatewayClassesExist() {
        assertThat(ApiGatewayApplication.class).isNotNull();
        assertThat(dev.makeev.gateway.controller.GatewayController.class).isNotNull();
        assertThat(dev.makeev.gateway.controller.FallbackController.class).isNotNull();
        assertThat(dev.makeev.gateway.filter.LoggingFilter.class).isNotNull();
    }
}
