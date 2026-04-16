package dev.makeev.inventory.listener;

import dev.makeev.common.events.ProductEvent;
import dev.makeev.common.events.EventType;
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

    public Mono<Void> handleProductEventAsync(ProductEvent event) {
        log.info("Processing product event asynchronously: {} for product: {}", event.eventType(), event.productId());
        
        return switch (event.eventType()) {
            case EventType.CREATED -> 
                inventoryService.createInventory(
                        new dev.makeev.inventory.model.Inventory(
                                event.productId(),
                                "Product " + event.productId(),
                                100,
                                "default",
                                "SKU-" + event.productId()
                        )
                ).then();
            case EventType.UPDATED -> 
                inventoryService.getInventoryByProductId(event.productId())
                        .flatMap(inventory -> {
                            inventory.setProductName("Product " + event.productId());
                            inventory.setSku("SKU-" + event.productId());
                            inventory.setUpdatedAt(java.time.Instant.now());
                            return inventoryService.updateInventoryQuantity(
                                    event.productId(), inventory.getQuantity());
                        })
                        .then();
            case EventType.DELETED -> 
                inventoryService.deleteInventory(event.productId());
        };
    }
}
