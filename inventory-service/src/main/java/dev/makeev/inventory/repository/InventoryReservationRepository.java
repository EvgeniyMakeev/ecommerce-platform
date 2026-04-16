package dev.makeev.inventory.repository;

import dev.makeev.inventory.model.InventoryReservation;
import dev.makeev.inventory.model.ReservationStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface InventoryReservationRepository extends ReactiveCrudRepository<InventoryReservation, Long> {

    Flux<InventoryReservation> findByOrderId(String orderId);

    Flux<InventoryReservation> findByStatus(ReservationStatus status);

    Mono<InventoryReservation> findByProductIdAndOrderId(String productId, String orderId);

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM inventory_reservations WHERE product_id = :productId AND status = 'PENDING'")
    Mono<Integer> sumPendingReservationsByProductId(String productId);
}
