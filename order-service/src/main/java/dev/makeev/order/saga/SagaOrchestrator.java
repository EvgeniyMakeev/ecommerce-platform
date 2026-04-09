package dev.makeev.order.saga;

import dev.makeev.order.client.InventoryServiceClient;
import dev.makeev.order.client.NotificationServiceClient;
import dev.makeev.order.client.PaymentServiceClient;
import dev.makeev.order.model.Order;
import dev.makeev.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final OrderService orderService;
    private final InventoryServiceClient inventoryServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public Mono<Order> executeOrderSaga(Order order) {
        String sagaId = UUID.randomUUID().toString();
        log.info("Starting order saga {} for order: {}", sagaId, order.getOrderNumber());
        
        return orderService.updateOrder(order.getId(), order)
                .flatMap(updatedOrder -> {
                    updatedOrder.startSaga(sagaId);
                    return orderService.updateOrder(order.getId(), updatedOrder);
                })
                .flatMap(updatedOrder -> reserveInventory(updatedOrder, sagaId))
                .flatMap(updatedOrder -> processPayment(updatedOrder, sagaId))
                .flatMap(updatedOrder -> confirmInventory(updatedOrder, sagaId))
                .flatMap(updatedOrder -> sendNotification(updatedOrder, sagaId))
                .flatMap(updatedOrder -> {
                    updatedOrder.completeSaga();
                    return orderService.updateOrder(order.getId(), updatedOrder);
                })
                .doOnSuccess(completedOrder -> 
                        log.info("Order saga {} completed successfully for order: {}", 
                                sagaId, completedOrder.getOrderNumber()))
                .doOnError(error -> {
                    log.error("Order saga {} failed for order {}: {}", 
                            sagaId, order.getOrderNumber(), error.getMessage());
                    compensateOrder(order, sagaId, error.getMessage()).subscribe();
                })
                .onErrorResume(error -> compensateOrder(order, sagaId, error.getMessage()));
    }

    private Mono<Order> reserveInventory(Order order, String sagaId) {
        log.info("Reserving inventory for order: {} (saga: {})", order.getOrderNumber(), sagaId);
        
        return Mono.fromCallable(() -> {
                    // Store compensation data for inventory reservation
                    String compensationData = buildInventoryCompensationData(order);
                    order.setCompensationData(compensationData);
                    return order;
                })
                .flatMap(updatedOrder -> orderService.updateOrder(updatedOrder.getId(), updatedOrder))
                .flatMap(updatedOrder -> 
                    inventoryServiceClient.reserveInventory(updatedOrder)
                            .map(response -> updatedOrder)
                            .onErrorResume(error -> {
                                log.error("Inventory reservation failed for order {}: {}", 
                                        order.getOrderNumber(), error.getMessage());
                                updatedOrder.failSaga("Inventory reservation failed: " + error.getMessage());
                                return orderService.updateOrder(updatedOrder.getId(), updatedOrder)
                                        .then(Mono.error(error));
                            }));
    }

    private Mono<Order> processPayment(Order order, String sagaId) {
        log.info("Processing payment for order: {} (saga: {})", order.getOrderNumber(), sagaId);
        
        return paymentServiceClient.processPayment(order)
                .flatMap(paymentResult -> {
                    if (paymentResult.success()) {
                        log.info("Payment processed successfully for order: {}", order.getOrderNumber());
                        return Mono.just(order);
                    } else {
                        log.error("Payment failed for order {}: {}", 
                                order.getOrderNumber(), paymentResult.errorMessage());
                        order.failSaga("Payment failed: " + paymentResult.errorMessage());
                        return orderService.updateOrder(order.getId(), order)
                                .then(Mono.error(new RuntimeException(paymentResult.errorMessage())));
                    }
                })
                .onErrorResume(error -> {
                    log.error("Payment processing error for order {}: {}", 
                            order.getOrderNumber(), error.getMessage());
                    order.failSaga("Payment processing error: " + error.getMessage());
                    return orderService.updateOrder(order.getId(), order)
                            .then(Mono.error(error));
                });
    }

    private Mono<Order> confirmInventory(Order order, String sagaId) {
        log.info("Confirming inventory for order: {} (saga: {})", order.getOrderNumber(), sagaId);
        
        return inventoryServiceClient.confirmInventoryReservation(order)
                .map(response -> order)
                .onErrorResume(error -> {
                    log.error("Inventory confirmation failed for order {}: {}", 
                            order.getOrderNumber(), error.getMessage());
                    order.failSaga("Inventory confirmation failed: " + error.getMessage());
                    return orderService.updateOrder(order.getId(), order)
                            .then(Mono.error(error));
                });
    }

    private Mono<Order> sendNotification(Order order, String sagaId) {
        log.info("Sending notification for order: {} (saga: {})", order.getOrderNumber(), sagaId);
        
        return notificationServiceClient.sendOrderConfirmation(order)
                .map(response -> order)
                .onErrorResume(error -> {
                    log.warn("Notification failed for order {} (non-critical): {}", 
                            order.getOrderNumber(), error.getMessage());
                    return Mono.just(order);
                });
    }

    private Mono<Order> compensateOrder(Order order, String sagaId, String failureReason) {
        log.info("Starting compensation for order: {} (saga: {})", order.getOrderNumber(), sagaId);
        
        return Mono.just(order)
                .flatMap(this::releaseInventoryReservation)
                .flatMap(this::refundPayment)
                .flatMap(this::sendCompensationNotification)
                .flatMap(compensatedOrder -> {
                    compensatedOrder.failSaga(failureReason);
                    return orderService.updateOrder(compensatedOrder.getId(), compensatedOrder);
                })
                .doOnSuccess(compensatedOrder -> 
                        log.info("Order compensation completed for order: {}", compensatedOrder.getOrderNumber()))
                .doOnError(error -> 
                        log.error("Order compensation failed for order {}: {}", 
                                order.getOrderNumber(), error.getMessage()));
    }

    private Mono<Order> releaseInventoryReservation(Order order) {
        if (order.getCompensationData() != null) {
            log.info("Releasing inventory reservation for order: {}", order.getOrderNumber());
            return inventoryServiceClient.releaseInventoryReservation(order)
                    .map(response -> order)
                    .onErrorResume(error -> {
                        log.error("Failed to release inventory reservation for order {}: {}", 
                                order.getOrderNumber(), error.getMessage());
                        return Mono.just(order); // Continue compensation even if this fails
                    });
        }
        return Mono.just(order);
    }

    private Mono<Order> refundPayment(Order order) {
        log.info("Refunding payment for order: {}", order.getOrderNumber());
        return paymentServiceClient.refundPayment(order)
                .map(response -> order)
                .onErrorResume(error -> {
                    log.error("Failed to refund payment for order {}: {}", 
                            order.getOrderNumber(), error.getMessage());
                    return Mono.just(order); // Continue compensation even if this fails
                });
    }

    private Mono<Order> sendCompensationNotification(Order order) {
        log.info("Sending compensation notification for order: {}", order.getOrderNumber());
        return notificationServiceClient.sendOrderCancellationNotification(order)
                .map(response -> order)
                .onErrorResume(error -> {
                    log.warn("Failed to send compensation notification for order {} (non-critical): {}", 
                            order.getOrderNumber(), error.getMessage());
                    return Mono.just(order); // Continue compensation even if this fails
                });
    }

    private String buildInventoryCompensationData(Order order) {
        StringBuilder data = new StringBuilder();
        data.append("{");
        data.append("\"orderId\":\"").append(order.getOrderNumber()).append("\",");
        data.append("\"items\":[");
        for (int i = 0; i < order.getItems().size(); i++) {
            var item = order.getItems().get(i);
            data.append("{\"productId\":\"").append(item.getProductId()).append("\",");
            data.append("\"quantity\":").append(item.getQuantity()).append("}");
            if (i < order.getItems().size() - 1) {
                data.append(",");
            }
        }
        data.append("]");
        data.append("}");
        return data.toString();
    }
}
