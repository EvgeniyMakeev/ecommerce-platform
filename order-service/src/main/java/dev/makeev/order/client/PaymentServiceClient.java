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
public class PaymentServiceClient {

    private final WebClient webClient;

    public Mono<PaymentResponse> processPayment(Order order) {
        log.info("Processing payment for order: {} amount: {}", 
                order.getOrderNumber(), order.getTotalAmount());
        
        PaymentRequest request = new PaymentRequest(
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                order.getCurrency(),
                "Order payment for " + order.getOrderNumber()
        );

        return webClient.post()
                .uri("/api/payments/process")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .doOnSuccess(response -> log.info("Payment processed for order {}: {}", 
                        order.getOrderNumber(), response.success() ? "SUCCESS" : "FAILED"))
                .doOnError(error -> log.error("Payment processing error for order {}: {}", 
                        order.getOrderNumber(), error.getMessage()));
    }

    public Mono<PaymentResponse> refundPayment(Order order) {
        log.info("Refunding payment for order: {} amount: {}", 
                order.getOrderNumber(), order.getTotalAmount());
        
        RefundRequest request = new RefundRequest(
                order.getOrderNumber(),
                order.getTotalAmount(),
                "Order refund for " + order.getOrderNumber()
        );

        return webClient.post()
                .uri("/api/payments/refund")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .doOnSuccess(response -> log.info("Refund processed for order {}: {}", 
                        order.getOrderNumber(), response.success() ? "SUCCESS" : "FAILED"))
                .doOnError(error -> log.error("Refund processing error for order {}: {}", 
                        order.getOrderNumber(), error.getMessage()));
    }

    public record PaymentResponse(boolean success, String transactionId, String errorMessage) {}

    public record PaymentRequest(
            String orderId,
            String userId,
            java.math.BigDecimal amount,
            String currency,
            String description
    ) {}

    public record RefundRequest(
            String orderId,
            java.math.BigDecimal amount,
            String reason
    ) {}
}
