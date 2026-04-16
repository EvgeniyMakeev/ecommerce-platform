package dev.makeev.inventory.service;

import dev.makeev.inventory.exception.InsufficientStockException;
import dev.makeev.inventory.exception.InventoryNotFoundException;
import dev.makeev.inventory.exception.ReservationAlreadyExistsException;
import dev.makeev.inventory.exception.ReservationNotActiveException;
import dev.makeev.inventory.exception.ReservationNotFoundException;
import dev.makeev.inventory.model.Inventory;
import dev.makeev.inventory.model.InventoryReservation;
import dev.makeev.inventory.model.ReservationStatus;
import dev.makeev.inventory.repository.InventoryRepository;
import dev.makeev.inventory.repository.InventoryReservationRepository;
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
    private final InventoryReservationRepository reservationRepository;

    public Mono<Inventory> getInventoryByProductId(String productId) {
        log.info("Getting inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .doOnSuccess(inventory -> log.debug("Found inventory for product {}: quantity {}", 
                        productId, inventory.getQuantity()))
                .doOnError(error -> log.error("Error getting inventory for product {}: {}", productId, error.toString()));
    }

    public Flux<Inventory> getInventoryByLocation(String location) {
        log.info("Getting inventory for location: {}", location);
        return inventoryRepository.findByLocation(location)
                .doOnComplete(() -> log.debug("Completed fetching inventory for location: {}", location))
                .doOnError(error -> log.error("Error getting inventory for location {}: {}", location, error.toString()));
    }

    public Flux<Inventory> getLowStockItems(int threshold) {
        log.info("Getting low stock items with threshold: {}", threshold);
        return inventoryRepository.findByQuantityLessThan(threshold)
                .doOnComplete(() -> log.debug("Completed fetching low stock items"))
                .doOnError(error -> log.error("Error getting low stock items: {}", error.toString()));
    }

    public Flux<Inventory> getOutOfStockItems() {
        log.info("Getting out of stock items");
        return inventoryRepository.findByQuantityLessThan(1)
                .doOnComplete(() -> log.debug("Completed fetching out of stock items"))
                .doOnError(error -> log.error("Error getting out of stock items: {}", error.toString()));
    }

    public Mono<Inventory> createInventory(Inventory inventory) {
        log.info("Creating inventory for product: {} at location: {}", 
                inventory.getProductId(), inventory.getLocation());
        return inventoryRepository.save(inventory)
                .doOnSuccess(saved -> log.info("Created inventory: {} for product: {}", 
                        saved.getId(), saved.getProductId()))
                .doOnError(error -> log.error("Error creating inventory: {}", error.toString()));
    }

    public Mono<Inventory> updateInventoryQuantity(String productId, int newQuantity) {
        log.info("Updating inventory quantity for product: {} to {}", productId, newQuantity);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    inventory.updateQuantity(newQuantity);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new InventoryNotFoundException(productId)))
                .doOnSuccess(updated -> log.info("Updated inventory for product: {} to quantity: {}", 
                        productId, newQuantity))
                .doOnError(error -> log.error("Error updating inventory for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> addStock(String productId, int amount) {
        log.info("Adding {} units to inventory for product: {}", amount, productId);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    inventory.addStock(amount);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new InventoryNotFoundException(productId)))
                .doOnSuccess(updated -> log.info("Added {} units to inventory for product: {}", 
                        amount, productId))
                .doOnError(error -> log.error("Error adding stock for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> removeStock(String productId, int amount) {
        log.info("Removing {} units from inventory for product: {}", amount, productId);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    if (!inventory.hasEnoughStock(amount)) {
                        return Mono.error(new InsufficientStockException(productId, inventory.getQuantity(), amount));
                    }
                    inventory.removeStock(amount);
                    return inventoryRepository.save(inventory);
                })
                .switchIfEmpty(Mono.error(new InventoryNotFoundException(productId)))
                .doOnSuccess(updated -> log.info("Removed {} units from inventory for product: {}", 
                        amount, productId))
                .doOnError(error -> log.error("Error removing stock for product {}: {}", productId, error.toString()));
    }

    public Mono<Boolean> isInStock(String productId) {
        log.info("Checking stock for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::isInStock)
                .defaultIfEmpty(false)
                .doOnSuccess(inStock -> log.debug("Product {} in stock: {}", productId, inStock))
                .doOnError(error -> log.error("Error checking stock for product {}: {}", productId, error.toString()));
    }

    public Mono<Boolean> hasEnoughStock(String productId, int requiredQuantity) {
        log.info("Checking if {} units available for product: {}", requiredQuantity, productId);
        return inventoryRepository.findByProductId(productId)
                .map(inventory -> inventory.hasEnoughStock(requiredQuantity))
                .defaultIfEmpty(false)
                .doOnSuccess(hasEnough -> log.debug("Product {} has {} units available: {}", 
                        productId, requiredQuantity, hasEnough))
                .doOnError(error -> log.error("Error checking stock availability for product {}: {}", productId, error.toString()));
    }

    public Mono<Void> deleteInventory(String productId) {
        log.info("Deleting inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventoryRepository::delete)
                .then()
                .doOnSuccess(v -> log.info("Deleted inventory for product: {}", productId))
                .doOnError(error -> log.error("Error deleting inventory for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> reserveStock(String productId, int quantity, String orderId) {
        log.info("Reserving {} units of product {} for order {}", quantity, productId, orderId);
        
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    if (!inventory.hasEnoughStock(quantity)) {
                        return Mono.error(new InsufficientStockException(productId, inventory.getQuantity(), quantity));
                    }
                    
                    return reservationRepository.findByProductIdAndOrderId(productId, orderId)
                            .flatMap(existing -> Mono.<Inventory>error(new ReservationAlreadyExistsException(orderId)))
                            .switchIfEmpty(Mono.defer(() -> {
                                inventory.removeStock(quantity);
                                return inventoryRepository.save(inventory)
                                        .flatMap(saved -> {
                                            InventoryReservation reservation = new InventoryReservation(productId, orderId, quantity, ReservationStatus.PENDING);
                                            return reservationRepository.save(reservation)
                                                    .thenReturn(saved);
                                        });
                            }));
                })
                .switchIfEmpty(Mono.error(new InventoryNotFoundException(productId)))
                .doOnSuccess(reserved -> log.info("Reserved {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Error reserving stock for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> releaseStock(String productId, int quantity, String orderId) {
        log.info("Releasing {} units of product {} from order {}", quantity, productId, orderId);
        
        return reservationRepository.findByProductIdAndOrderId(productId, orderId)
                .flatMap(reservation -> {
                    if (!reservation.isActive()) {
                        return Mono.error(new ReservationNotActiveException(orderId));
                    }
                    return inventoryRepository.findByProductId(productId)
                            .flatMap(inventory -> {
                                inventory.addStock(quantity);
                                return inventoryRepository.save(inventory)
                                        .flatMap(saved -> reservationRepository.delete(reservation).thenReturn(saved));
                            });
                })
                .switchIfEmpty(Mono.error(new ReservationNotFoundException(orderId)))
                .doOnSuccess(released -> log.info("Released {} units of product {} from order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Error releasing stock for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> confirmReservation(String productId, int quantity, String orderId) {
        log.info("Confirming reservation of {} units of product {} for order {}", quantity, productId, orderId);
        
        return reservationRepository.findByProductIdAndOrderId(productId, orderId)
                .flatMap(reservation -> {
                    if (!reservation.isActive()) {
                        return Mono.error(new ReservationNotActiveException(orderId));
                    }
                    reservation.confirm();
                    return reservationRepository.save(reservation)
                            .flatMap(saved -> inventoryRepository.findByProductId(productId));
                })
                .switchIfEmpty(Mono.error(new ReservationNotFoundException(orderId)))
                .doOnSuccess(confirmed -> log.info("Confirmed reservation of {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Error confirming reservation for product {}: {}", productId, error.toString()));
    }

    public Mono<Inventory> cancelReservation(String productId, int quantity, String orderId) {
        log.info("Cancelling reservation of {} units of product {} for order {}", quantity, productId, orderId);
        
        return reservationRepository.findByProductIdAndOrderId(productId, orderId)
                .flatMap(reservation -> {
                    if (!reservation.isActive()) {
                        return Mono.error(new ReservationNotActiveException(orderId));
                    }
                    reservation.cancel();
                    return reservationRepository.save(reservation)
                            .flatMap(saved -> inventoryRepository.findByProductId(productId)
                                    .flatMap(inventory -> {
                                        inventory.addStock(quantity);
                                        return inventoryRepository.save(inventory);
                                    }));
                })
                .switchIfEmpty(Mono.error(new ReservationNotFoundException(orderId)))
                .doOnSuccess(cancelled -> log.info("Cancelled reservation of {} units of product {} for order {}", 
                        quantity, productId, orderId))
                .doOnError(error -> log.error("Error cancelling reservation for product {}: {}", productId, error.toString()));
    }

    public Flux<Inventory> getReservedItems() {
        log.info("Getting all reserved inventory items");
        return reservationRepository.findByStatus(ReservationStatus.PENDING)
                .map(InventoryReservation::getProductId)
                .distinct()
                .flatMap(inventoryRepository::findByProductId)
                .doOnComplete(() -> log.debug("Completed fetching reserved items"))
                .doOnError(error -> log.error("Error getting reserved items: {}", error.toString()));
    }

    public Flux<Inventory> getReservedItemsByOrder(String orderId) {
        log.info("Getting reserved items for order: {}", orderId);
        return reservationRepository.findByOrderId(orderId)
                .filter(InventoryReservation::isActive)
                .map(InventoryReservation::getProductId)
                .flatMap(inventoryRepository::findByProductId)
                .doOnComplete(() -> log.debug("Completed fetching reserved items for order: {}", orderId))
                .doOnError(error -> log.error("Error getting reserved items for order {}: {}", orderId, error.toString()));
    }

    public Mono<Integer> getAvailableStock(String productId) {
        log.info("Getting available stock for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .flatMap(inventory -> {
                    int totalQuantity = inventory.getQuantity();
                    return reservationRepository.sumPendingReservationsByProductId(productId)
                            .map(reserved -> totalQuantity - reserved)
                            .defaultIfEmpty(totalQuantity);
                })
                .defaultIfEmpty(0)
                .doOnSuccess(available -> log.debug("Product {} has {} units available", productId, available))
                .doOnError(error -> log.error("Error getting available stock for product {}: {}", productId, error.toString()));
    }
}
