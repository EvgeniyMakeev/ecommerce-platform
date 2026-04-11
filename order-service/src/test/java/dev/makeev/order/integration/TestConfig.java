package dev.makeev.order.integration;

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
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Bean
    @Primary
    public InventoryServiceClient mockInventoryServiceClient() {
        return Mockito.mock(InventoryServiceClient.class);
    }

    @Bean
    @Primary
    public PaymentServiceClient mockPaymentServiceClient() {
        PaymentServiceClient mockClient = Mockito.mock(PaymentServiceClient.class);
        // Mock successful payment response
        when(mockClient.processPayment(any()))
                .thenReturn(Mono.just(new PaymentServiceClient.PaymentResponse(true, "txn-123", null)));
        when(mockClient.refundPayment(any()))
                .thenReturn(Mono.just(new PaymentServiceClient.PaymentResponse(true, "refund-123", null)));
        return mockClient;
    }

    @Bean
    @Primary
    public NotificationServiceClient mockNotificationServiceClient() {
        NotificationServiceClient mockClient = Mockito.mock(NotificationServiceClient.class);
        when(mockClient.sendOrderConfirmation(any()))
                .thenReturn(Mono.just(new NotificationServiceClient.NotificationResponse(true, "notif-123", "Notification sent")));
        when(mockClient.sendOrderCancellationNotification(any()))
                .thenReturn(Mono.just(new NotificationServiceClient.NotificationResponse(true, "notif-456", "Cancellation notification sent")));
        return mockClient;
    }
}
