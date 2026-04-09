package dev.makeev.inventory.service;

import dev.makeev.inventory.model.Inventory;
import dev.makeev.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Mono<Inventory> getInventoryByProductId(String productId) {
        log.info("Getting inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .doOnSuccess(inventory -> log.debug("Found inventory for product {}: quantity {}", 
                        productId, inventory.getQuantity()))
                .doOnError(error -> log.error("Error getting inventory for product {}: {}", productId, error));
    }

    public Flux<Inventory> getInventoryByLocation(String location) {
        log.info("Getting inventory for location: {}", location);
        return inventoryRepository.findByLocation(location)
                .doOnComplete(() -> log.debug("Completed fetching inventory for location: {}", location))
                .doOnError(error -> log.error("Error getting inventory for location {}: {}", location, error));
    }

    public Flux<Inventory> getLowStockItems(int threshold) {
        log.info("Getting low stock items with threshold: {}", threshold);
        return inventoryRepository.findByQuantityLessThan(threshold)
                .doOnComplete(() -> log.debug("Completed fetching low stock items"))
                .doOnError(error -> log.error("Error getting low stock items: {}", error));
    }

    public Flux<Inventory> getOutOfStockItems() {
        log.info("Getting out of stock items");
        return inventoryRepository.findByQuantityLessThan(1)
                .doOnComplete(() -> log.debug("Completed fetching out of stock items"))
                .doOnError(error -> log.error("Error getting out of stock items: {}", error));
    }

    public Mono<Inventory> createInventory(Inventory inventory) {
        log.info("Creating inventory for product: {} at location: {}", 
                inventory.getProductId(), inventory.getLocation());
        return inventoryRepository.save(inventory)
                .doOnSuccess(saved -> log.info("Created inventory: {} for product: {}", 
                        saved.getId(), saved.getProductId()))
                .doOnError(error -> log.error("Error creating inventory: {}", error));
    }

    public Mono<Inventory> updateInventoryQuantity(String productId, int newQuantity) {
        log.info("Updating inventory quantity for product: {} to {}", productId, newQuantity);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    inventory.updateQuantity(newQuantity);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Inventory not found for product: " + productId)))
                .doOnSuccess(updated -> log.info("Updated inventory for product: {} to quantity: {}", 
                        productId, newQuantity))
                .doOnError(error -> log.error("Error updating inventory for product {}: {}", productId, error));
    }

    public Mono<Inventory> addStock(String productId, int amount) {
        log.info("Adding {} units to inventory for product: {}", amount, productId);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    inventory.addStock(amount);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Inventory not found for product: " + productId)))
                .doOnSuccess(updated -> log.info("Added {} units to inventory for product: {}", 
                        amount, productId))
                .doOnError(error -> log.error("Error adding stock for product {}: {}", productId, error));
    }

    public Mono<Inventory> removeStock(String productId, int amount) {
        log.info("Removing {} units from inventory for product: {}", amount, productId);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    if (!inventory.hasEnoughStock(amount)) {
                        return Mono.error(new RuntimeException("Insufficient stock for product: " + productId));
                    }
                    inventory.removeStock(amount);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Inventory not found for product: " + productId)))
                .doOnSuccess(updated -> log.info("Removed {} units from inventory for product: {}", 
                        amount, productId))
                .doOnError(error -> log.error("Error removing stock for product {}: {}", productId, error));
    }

    public Mono<Boolean> isInStock(String productId) {
        log.info("Checking stock for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::isInStock)
                .defaultIfEmpty(false)
                .doOnSuccess(inStock -> log.debug("Product {} in stock: {}", productId, inStock))
                .doOnError(error -> log.error("Error checking stock for product {}: {}", productId, error));
    }

    public Mono<Boolean> hasEnoughStock(String productId, int requiredQuantity) {
        log.info("Checking if {} units available for product: {}", requiredQuantity, productId);
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.hasEnoughStock(requiredQuantity))
                .defaultIfEmpty(false)
                .doOnSuccess(hasEnough -> log.debug("Product {} has {} units available: {}", 
                        productId, requiredQuantity, hasEnough))
                .doOnError(error -> log.error("Error checking stock availability for product {}: {}", productId, error));
    }

    public Mono<Void> deleteInventory(String productId) {
        log.info("Deleting inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventoryRepository::delete)
                .then()
                .doOnSuccess(v -> log.info("Deleted inventory for product: {}", productId))
                .doOnError(error -> log.error("Error deleting inventory for product {}: {}", productId, error));
    }
}
