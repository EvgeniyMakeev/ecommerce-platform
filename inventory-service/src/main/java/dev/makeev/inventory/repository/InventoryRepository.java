package dev.makeev.inventory.repository;

import dev.makeev.inventory.model.Inventory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryRepository extends R2dbcRepository<Inventory, Long> {

    Mono<Inventory> findByProductId(String productId);

    Flux<Inventory> findByLocation(String location);

    Flux<Inventory> findByQuantityGreaterThan(int minQuantity);

    Flux<Inventory> findByQuantityLessThan(int maxQuantity);

    Flux<Inventory> findBySku(String sku);

    Mono<Inventory> findByProductIdAndLocation(String productId, String location);
}
