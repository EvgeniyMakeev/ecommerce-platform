package dev.makeev.cart.repository;

import dev.makeev.cart.model.Cart;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CartRepository {

    Mono<Cart> save(Cart cart);

    Mono<Cart> findById(String id);

    Mono<Cart> findByUserId(String userId);

    Flux<Cart> findByUserIdOrderByUpdatedAtDesc(String userId);

    Mono<Void> deleteById(String id);

    Mono<Void> deleteByUserId(String userId);

    Flux<Cart> findAll();
}
