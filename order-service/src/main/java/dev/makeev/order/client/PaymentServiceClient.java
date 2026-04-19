package dev.makeev.order.client;

import dev.makeev.order.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
public class PaymentServiceClient {

    public Mono<PaymentResponse> processPayment(Order order) {
        log.info("MOCK: Processing payment for order: {} amount: {}",
                order.getOrderNumber(), order.getTotalAmount());

        return Mono.just(new PaymentResponse(
                true,
                "txn-" + UUID.randomUUID(),
                null
        ));
    }

    public Mono<PaymentResponse> refundPayment(Order order) {
        log.info("MOCK: Refunding payment for order: {} amount: {}",
                order.getOrderNumber(), order.getTotalAmount());

        return Mono.just(new PaymentResponse(
                true,
                "refund-" + UUID.randomUUID(),
                null
        ));
    }
}
