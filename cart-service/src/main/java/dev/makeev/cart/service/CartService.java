package dev.makeev.cart.service;

import dev.makeev.cart.model.Cart;
import dev.makeev.cart.model.CartItem;
import dev.makeev.cart.repository.CartRepository;
import dev.makeev.common.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;

    public Mono<Cart> getCartByUserId(String userId) {
        log.info("Getting cart for user: {}", userId);
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cart not found for user: " + userId)))
                .doOnSuccess(cart -> log.debug("Found cart for user {}: {} items", userId, cart.getItemCount()));
    }

    public Mono<Cart> addItemToCart(String userId, CartItem item) {
        log.info("Adding item to cart for user {}: productId {}", userId, item.getProductId());

        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Creating new cart for user: {}", userId);
                    return Mono.just(new Cart(userId));
                }))
                .flatMap(cart -> {
                    cart.addItem(item);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(cart -> log.info("Added item to cart: {} for user: {}", item.getProductId(), userId))
                .doOnError(error -> log.error("Error adding item to cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Cart> removeItemFromCart(String userId, String productId) {
        log.info("Removing item from cart for user {}: productId {}", userId, productId);
        
        return getCartByUserId(userId)
                .flatMap(cart -> {
                    cart.removeItem(productId);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(cart -> log.info("Removed item from cart: {} for user: {}", productId, userId))
                .doOnError(error -> log.error("Error removing item from cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Cart> updateItemInCart(String userId, String productId, CartItem item) {
        log.info("Updating item in cart for user {}: productId {}", userId, productId);
        
        return getCartByUserId(userId)
                .flatMap(cart -> {
                    cart.updateItem(productId, item);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(cart -> log.info("Updated item in cart: {} for user: {}", productId, userId))
                .doOnError(error -> log.error("Error updating item in cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Cart> clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        
        return getCartByUserId(userId)
                .flatMap(cart -> {
                    cart.clear();
                    return cartRepository.save(cart);
                })
                .doOnSuccess(cart -> log.info("Cleared cart for user: {}", userId))
                .doOnError(error -> log.error("Error clearing cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Void> deleteCart(String userId) {
        log.info("Deleting cart for user: {}", userId);
        
        return cartRepository.deleteByUserId(userId)
                .doOnSuccess(v -> log.info("Deleted cart for user: {}", userId))
                .doOnError(error -> log.error("Error deleting cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Cart> addProductToCart(String userId, ProductDTO product, int quantity) {
        log.info("Adding product to cart for user {}: productId {}", userId, product.id());
        
        CartItem item = new CartItem(
                product.id(),
                product.name(),
                product.price(),
                quantity,
                product.imageUrl()
        );
        
        return addItemToCart(userId, item);
    }
}
