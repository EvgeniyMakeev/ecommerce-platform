package dev.makeev.order.config;

import dev.makeev.order.client.InventoryServiceClient;
import dev.makeev.order.client.NotificationServiceClient;
import dev.makeev.order.client.PaymentServiceClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public WebClient testWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8085")
                .build();
    }

    @Bean
    @Primary
    public InventoryServiceClient mockInventoryServiceClient() {
        InventoryServiceClient mockClient = Mockito.mock(InventoryServiceClient.class);
        
        when(mockClient.reserveInventory(any()))
                .thenReturn(Mono.just(new InventoryServiceClient.InventoryResponse(true, "Inventory reserved")));
        
        when(mockClient.confirmInventoryReservation(any()))
                .thenReturn(Mono.just(new InventoryServiceClient.InventoryResponse(true, "Inventory confirmed")));
        
        when(mockClient.releaseInventoryReservation(any()))
                .thenReturn(Mono.just(new InventoryServiceClient.InventoryResponse(true, "Inventory released")));
        
        return mockClient;
    }

    @Bean
    @Primary
    public PaymentServiceClient mockPaymentServiceClient() {
        PaymentServiceClient mockClient = Mockito.mock(PaymentServiceClient.class);
        
        when(mockClient.processPayment(any()))
                .thenReturn(Mono.just(new PaymentServiceClient.PaymentResponse(true, "payment-123", null)));
        
        when(mockClient.refundPayment(any()))
                .thenReturn(Mono.just(new PaymentServiceClient.PaymentResponse(true, "refund-123", null)));
        
        return mockClient;
    }

    @Bean
    @Primary
    public NotificationServiceClient mockNotificationServiceClient() {
        NotificationServiceClient mockClient = Mockito.mock(NotificationServiceClient.class);
        
        when(mockClient.sendOrderConfirmation(any()))
                .thenReturn(Mono.just(new NotificationServiceClient.NotificationResponse(true, "notif-123", "Sent")));
        
        when(mockClient.sendOrderCancellationNotification(any()))
                .thenReturn(Mono.just(new NotificationServiceClient.NotificationResponse(true, "notif-456", "Sent")));
        
        return mockClient;
    }
}
