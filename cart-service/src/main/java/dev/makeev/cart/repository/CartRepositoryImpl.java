package dev.makeev.cart.repository;

import dev.makeev.cart.model.Cart;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class CartRepositoryImpl implements CartRepository {

    private final ReactiveRedisTemplate<String, Cart> redisTemplate;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final String USER_CART_KEY_PREFIX = "user_cart:";

    public CartRepositoryImpl(ReactiveRedisTemplate<String, Cart> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        String key = CART_KEY_PREFIX + cart.getId();
        String userKey = USER_CART_KEY_PREFIX + cart.getUserId();
        
        return redisTemplate.opsForValue().set(key, cart, Duration.ofHours(24))
                .then(redisTemplate.opsForValue().set(userKey, cart, Duration.ofHours(24)))
                .then(Mono.just(cart));
    }

    @Override
    public Mono<Cart> findById(String id) {
        return redisTemplate.opsForValue().get(CART_KEY_PREFIX + id);
    }

    @Override
    public Mono<Cart> findByUserId(String userId) {
        return redisTemplate.opsForValue().get(USER_CART_KEY_PREFIX + userId);
    }

    @Override
    public Flux<Cart> findByUserIdOrderByUpdatedAtDesc(String userId) {
        return findByUserId(userId).flux();
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return findById(id)
                .flatMap(cart -> {
                    String key = CART_KEY_PREFIX + id;
                    String userKey = USER_CART_KEY_PREFIX + cart.getUserId();
                    return redisTemplate.delete(key)
                            .then(redisTemplate.delete(userKey))
                            .then();
                });
    }

    @Override
    public Mono<Void> deleteByUserId(String userId) {
        return findByUserId(userId)
                .flatMap(cart -> deleteById(cart.getId()))
                .then();
    }

    @Override
    public Flux<Cart> findAll() {
        return redisTemplate.keys(CART_KEY_PREFIX + "*")
                .flatMap(redisTemplate.opsForValue()::get);
    }
}
