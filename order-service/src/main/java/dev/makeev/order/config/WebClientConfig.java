package dev.makeev.order.config;

import dev.makeev.order.client.InventoryServiceClient;
import dev.makeev.order.client.NotificationServiceClient;
import dev.makeev.order.client.PaymentServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient inventoryWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8084")
                .build();
    }

    @Bean
    public WebClient paymentWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8085")
                .build();
    }

    @Bean
    public WebClient notificationWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8086")
                .build();
    }

    @Bean
    public InventoryServiceClient inventoryServiceClient(WebClient inventoryWebClient) {
        return new InventoryServiceClient(inventoryWebClient);
    }

    @Bean
    public PaymentServiceClient paymentServiceClient(WebClient paymentWebClient) {
        return new PaymentServiceClient(paymentWebClient);
    }

    @Bean
    public NotificationServiceClient notificationServiceClient(WebClient notificationWebClient) {
        return new NotificationServiceClient(notificationWebClient);
    }
}
