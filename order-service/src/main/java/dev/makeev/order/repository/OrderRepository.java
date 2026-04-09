package dev.makeev.order.repository;

import dev.makeev.order.model.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Mono<Order> findByOrderNumber(String orderNumber);

    Flux<Order> findByUserId(String userId);

    Flux<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Flux<Order> findByStatus(String status);

    Mono<Order> findByUserIdAndOrderNumber(String userId, String orderNumber);
}
