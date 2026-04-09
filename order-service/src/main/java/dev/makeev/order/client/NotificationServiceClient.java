package dev.makeev.order.client;

import dev.makeev.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationServiceClient {

    private final WebClient webClient;

    public Mono<NotificationResponse> sendOrderConfirmation(Order order) {
        log.info("Sending order confirmation notification for order: {}", order.getOrderNumber());
        
        NotificationRequest request = new NotificationRequest(
                order.getUserId(),
                "order_confirmation",
                "Your order " + order.getOrderNumber() + " has been confirmed!",
                buildOrderConfirmationMessage(order)
        );

        return webClient.post()
                .uri("/api/notifications/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .doOnSuccess(response -> log.info("Order confirmation sent for order: {}", order.getOrderNumber()))
                .doOnError(error -> log.error("Failed to send order confirmation for order {}: {}", 
                        order.getOrderNumber(), error.getMessage()));
    }

    public Mono<NotificationResponse> sendOrderCancellationNotification(Order order) {
        log.info("Sending order cancellation notification for order: {}", order.getOrderNumber());
        
        NotificationRequest request = new NotificationRequest(
                order.getUserId(),
                "order_cancellation",
                "Your order " + order.getOrderNumber() + " has been cancelled.",
                buildOrderCancellationMessage(order)
        );

        return webClient.post()
                .uri("/api/notifications/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(NotificationResponse.class)
                .doOnSuccess(response -> log.info("Order cancellation notification sent for order: {}", order.getOrderNumber()))
                .doOnError(error -> log.error("Failed to send order cancellation notification for order {}: {}", 
                        order.getOrderNumber(), error.getMessage()));
    }

    private String buildOrderConfirmationMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("Thank you for your order!\n\n");
        message.append("Order Number: ").append(order.getOrderNumber()).append("\n");
        message.append("Total Amount: ").append(order.getCurrency()).append(" ").append(order.getTotalAmount()).append("\n\n");
        message.append("Items:\n");
        
        for (var item : order.getItems()) {
            message.append("- ").append(item.getProductName())
                   .append(" x ").append(item.getQuantity())
                   .append(" = ").append(order.getCurrency()).append(" ")
                   .append(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                   .append("\n");
        }
        
        message.append("\nShipping Address: ").append(order.getShippingAddress()).append("\n");
        message.append("We'll notify you when your order ships.");
        
        return message.toString();
    }

    private String buildOrderCancellationMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("Your order ").append(order.getOrderNumber()).append(" has been cancelled.\n\n");
        message.append("Total Amount: ").append(order.getCurrency()).append(" ").append(order.getTotalAmount()).append("\n");
        message.append("A refund has been processed to your original payment method.\n\n");
        
        if (order.getFailureReason() != null && !order.getFailureReason().trim().isEmpty()) {
            message.append("Reason: ").append(order.getFailureReason()).append("\n\n");
        }
        
        message.append("If you have any questions, please contact our support team.");
        
        return message.toString();
    }

    public record NotificationResponse(boolean success, String notificationId, String message) {}

    public record NotificationRequest(
            String userId,
            String type,
            String subject,
            String message
    ) {}
}
