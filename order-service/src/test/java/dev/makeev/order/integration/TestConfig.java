package dev.makeev.order.integration;

import dev.makeev.order.client.InventoryServiceClient;
import dev.makeev.order.client.NotificationServiceClient;
import dev.makeev.order.client.PaymentServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public InventoryServiceClient mockInventoryServiceClient() {
        return Mockito.mock(InventoryServiceClient.class);
    }

    @Bean
    @Primary
    public PaymentServiceClient paymentServiceClient() {
        return new PaymentServiceClient();
    }

    @Bean
    @Primary
    public NotificationServiceClient notificationServiceClient() {
        return new NotificationServiceClient();
    }
}
