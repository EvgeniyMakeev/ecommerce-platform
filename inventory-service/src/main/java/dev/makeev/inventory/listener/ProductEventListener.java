package dev.makeev.inventory.listener;

import dev.makeev.common.events.ProductEvent;
import dev.makeev.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final InventoryService inventoryService;

    public void handleProductEvent(ProductEvent event) {
        log.info("Processing product event: {} for product: {}", event.eventType(), event.productId());
        
        switch (event.eventType()) {
            case ProductEvent.EventType.CREATED -> {
                log.info("Creating inventory for new product: {}", event.productId());
                inventoryService.createInventory(
                        new dev.makeev.inventory.model.Inventory(
                                event.productId(),
                                "Product " + event.productId(),
                                100,
                                "default",
                                "SKU-" + event.productId()
                        )
                ).doOnSuccess(inventory -> log.info("Created inventory for product: {}", event.productId()))
                .doOnError(error -> log.error("Error creating inventory for product {}: {}", 
                        event.productId(), error.getMessage()))
                .subscribe();
            }
            case ProductEvent.EventType.UPDATED -> {
                log.info("Product updated: {}", event.productId());
                inventoryService.getInventoryByProductId(event.productId())
                        .flatMap(inventory -> {
                            inventory.setProductName("Product " + event.productId());
                            inventory.setSku("SKU-" + event.productId());
                            inventory.setUpdatedAt(java.time.Instant.now());
                            return inventoryService.updateInventoryQuantity(
                                    event.productId(), inventory.getQuantity());
                        })
                        .doOnSuccess(inventory -> log.info("Updated inventory for product: {}", event.productId()))
                        .doOnError(error -> log.error("Error updating inventory for product {}: {}", 
                                event.productId(), error.getMessage()))
                        .subscribe();
            }
            case ProductEvent.EventType.DELETED -> {
                log.info("Product deleted: {}", event.productId());
                inventoryService.deleteInventory(event.productId())
                        .doOnSuccess(v -> log.info("Deleted inventory for product: {}", event.productId()))
                        .doOnError(error -> log.error("Error deleting inventory for product {}: {}", 
                                event.productId(), error.getMessage()))
                        .subscribe();
            }
        }
    }

    public Mono<Void> handleProductEventAsync(ProductEvent event) {
        log.info("Processing product event asynchronously: {} for product: {}", event.eventType(), event.productId());
        
        return switch (event.eventType()) {
            case ProductEvent.EventType.CREATED -> 
                inventoryService.createInventory(
                        new dev.makeev.inventory.model.Inventory(
                                event.productId(),
                                "Product " + event.productId(),
                                100,
                                "default",
                                "SKU-" + event.productId()
                        )
                ).then();
            case ProductEvent.EventType.UPDATED -> 
                inventoryService.getInventoryByProductId(event.productId())
                        .flatMap(inventory -> {
                            inventory.setProductName("Product " + event.productId());
                            inventory.setSku("SKU-" + event.productId());
                            inventory.setUpdatedAt(java.time.Instant.now());
                            return inventoryService.updateInventoryQuantity(
                                    event.productId(), inventory.getQuantity());
                        })
                        .then();
            case ProductEvent.EventType.DELETED -> 
                inventoryService.deleteInventory(event.productId());
        };
    }
}
