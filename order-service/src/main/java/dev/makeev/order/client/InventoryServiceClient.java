package dev.makeev.order.client;

import dev.makeev.order.dto.CancelReservationRequest;
import dev.makeev.order.dto.ConfirmReservationRequest;
import dev.makeev.order.dto.InventoryResponse;
import dev.makeev.order.dto.ReserveRequest;
import dev.makeev.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class InventoryServiceClient {

    private final WebClient webClient;

    public Mono<InventoryResponse> reserveInventory(Order order) {
        log.info("Reserving inventory for order: {}", order.getOrderNumber());
        
        return order.getItems().stream()
                .map(item -> reserveStock(item.getProductId(), item.getQuantity(), order.getOrderNumber()))
                .reduce(Mono.empty(), Mono::then)
                .then(Mono.just(new InventoryResponse(true, "Inventory reserved successfully")))
                .onErrorReturn(new InventoryResponse(false, "Inventory reservation failed"));
    }

    public Mono<InventoryResponse> confirmInventoryReservation(Order order) {
        log.info("Confirming inventory reservation for order: {}", order.getOrderNumber());
        
        return order.getItems().stream()
                .map(item -> confirmReservation(item.getProductId(), item.getQuantity(), order.getOrderNumber()))
                .reduce(Mono.empty(), Mono::then)
                .then(Mono.just(new InventoryResponse(true, "Inventory reservation confirmed")))
                .onErrorReturn(new InventoryResponse(false, "Inventory confirmation failed"));
    }

    public Mono<InventoryResponse> releaseInventoryReservation(Order order) {
        log.info("Releasing inventory reservation for order: {}", order.getOrderNumber());
        
        return order.getItems().stream()
                .map(item -> cancelReservation(item.getProductId(), item.getQuantity(), order.getOrderNumber()))
                .reduce(Mono.empty(), Mono::then)
                .then(Mono.just(new InventoryResponse(true, "Inventory reservation released")))
                .onErrorReturn(new InventoryResponse(false, "Inventory reservation release failed"));
    }

    private Mono<Void> reserveStock(String productId, int quantity, String orderId) {
        return webClient.post()
                .uri("/api/inventory/reserve")
                .bodyValue(new ReserveRequest(productId, quantity, orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.debug("Reserved {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Failed to reserve {} units of product {} for order {}: {}", 
                        quantity, productId, orderId, error.getMessage()));
    }

    private Mono<Void> confirmReservation(String productId, int quantity, String orderId) {
        return webClient.post()
                .uri("/api/inventory/confirm-reservation")
                .bodyValue(new ConfirmReservationRequest(productId, quantity, orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.debug("Confirmed reservation of {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Failed to confirm reservation of {} units of product {} for order {}: {}", 
                        quantity, productId, orderId, error.getMessage()));
    }

    private Mono<Void> cancelReservation(String productId, int quantity, String orderId) {
        return webClient.post()
                .uri("/api/inventory/cancel-reservation")
                .bodyValue(new CancelReservationRequest(productId, quantity, orderId))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.debug("Cancelled reservation of {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Failed to cancel reservation of {} units of product {} for order {}: {}",
                        quantity, productId, orderId, error.getMessage()));
    }
}
