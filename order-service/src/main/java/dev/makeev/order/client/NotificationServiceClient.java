package dev.makeev.order.client;

import dev.makeev.order.dto.NotificationResponse;
import dev.makeev.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class NotificationServiceClient {

    public Mono<NotificationResponse> sendOrderConfirmation(Order order) {
        log.info("MOCK: Sending order confirmation notification for order: {}", order.getOrderNumber());

        return Mono.just(new NotificationResponse(
                true,
                "notif-" + UUID.randomUUID(),
                "Order confirmation sent"
        ));
    }

    public Mono<NotificationResponse> sendOrderCancellationNotification(Order order) {
        log.info("MOCK: Sending order cancellation notification for order: {}", order.getOrderNumber());

        return Mono.just(new NotificationResponse(
                true,
                "notif-" + UUID.randomUUID(),
                "Order cancellation notification sent"
        ));
    }
}
