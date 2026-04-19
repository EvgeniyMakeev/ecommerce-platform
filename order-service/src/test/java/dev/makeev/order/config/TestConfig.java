package dev.makeev.order.config;

import dev.makeev.order.client.InventoryResponse;
import dev.makeev.order.client.InventoryServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public InventoryServiceClient mockInventoryServiceClient() {
        InventoryServiceClient mockClient = Mockito.mock(InventoryServiceClient.class);

        when(mockClient.reserveInventory(any()))
                .thenReturn(Mono.just(new InventoryResponse(true, "Inventory reserved")));

        when(mockClient.confirmInventoryReservation(any()))
                .thenReturn(Mono.just(new InventoryResponse(true, "Inventory confirmed")));

        when(mockClient.releaseInventoryReservation(any()))
                .thenReturn(Mono.just(new InventoryResponse(true, "Inventory released")));

        return mockClient;
    }
}
