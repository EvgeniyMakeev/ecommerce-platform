package dev.makeev.order.config;

import dev.makeev.order.client.InventoryServiceClient;
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
    public InventoryServiceClient inventoryServiceClient(WebClient inventoryWebClient) {
        return new InventoryServiceClient(inventoryWebClient);
    }
}
