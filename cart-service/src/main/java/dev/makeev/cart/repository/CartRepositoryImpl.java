package dev.makeev.cart.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.makeev.cart.model.Cart;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class CartRepositoryImpl implements CartRepository {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CART_KEY_PREFIX = "cart:";
    private static final String USER_CART_KEY_PREFIX = "user_cart:";

    public CartRepositoryImpl(ReactiveStringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        String key = CART_KEY_PREFIX + cart.getId();
        String userKey = USER_CART_KEY_PREFIX + cart.getUserId();
        
        try {
            String json = objectMapper.writeValueAsString(cart);
            return redisTemplate.opsForValue().set(key, json, Duration.ofHours(24))
                    .then(redisTemplate.opsForValue().set(userKey, json, Duration.ofHours(24)))
                    .then(Mono.just(cart));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<Cart> findById(String id) {
        return redisTemplate.opsForValue().get(CART_KEY_PREFIX + id)
                .map(this::deserializeCart);
    }

    @Override
    public Mono<Cart> findByUserId(String userId) {
        return redisTemplate.opsForValue().get(USER_CART_KEY_PREFIX + userId)
                .map(this::deserializeCart);
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
                .flatMap(key -> redisTemplate.opsForValue().get(key)
                        .map(this::deserializeCart));
    }

    private Cart deserializeCart(String json) {
        try {
            return objectMapper.readValue(json, Cart.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize Cart", e);
        }
    }
}
