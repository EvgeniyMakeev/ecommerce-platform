package dev.makeev.cart.controller;

import dev.makeev.cart.dto.AddItemRequest;
import dev.makeev.cart.dto.AddProductRequest;
import dev.makeev.cart.dto.UpdateItemRequest;
import dev.makeev.cart.model.Cart;
import dev.makeev.cart.model.CartItem;
import dev.makeev.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public Mono<ResponseEntity<Cart>> getCart(@PathVariable String userId) {
        log.info("REST: Get cart request - userId: {}", userId);

        return cartService.getCartByUserId(userId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/items")
    public Mono<ResponseEntity<Cart>> addItem(
            @PathVariable String userId,
            @Valid @RequestBody AddItemRequest request) {
        log.info("REST: Add item to cart - userId: {}, productId: {}, quantity: {}",
                userId, request.productId(), request.quantity());

        return cartService.addItemToCart(userId,
                        new CartItem(request.productId(), request.productName(),
                                request.price(), request.quantity(), request.imageUrl()))
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{userId}/items/{productId}")
    public Mono<ResponseEntity<Cart>> updateItem(
            @PathVariable String userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateItemRequest request) {
        log.info("REST: Update item in cart - userId: {}, productId: {}, quantity: {}",
                userId, productId, request.quantity());

        return cartService.updateItemInCart(userId, productId,
                        new CartItem(productId, request.productName(),
                                request.price(), request.quantity(), request.imageUrl()))
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public Mono<ResponseEntity<Void>> removeItem(@PathVariable String userId, @PathVariable String productId) {
        log.info("REST: Remove item from cart - userId: {}, productId: {}", userId, productId);

        return cartService.removeItemFromCart(userId, productId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{userId}/clear")
    public Mono<ResponseEntity<Void>> clearCart(@PathVariable String userId) {
        log.info("REST: Clear cart - userId: {}", userId);

        return cartService.clearCart(userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{userId}/products")
    public Mono<ResponseEntity<Cart>> addProductToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddProductRequest request) {
        log.info("REST: Add product to cart - userId: {}, productId: {}, quantity: {}",
                userId, request.product().id(), request.quantity());

        return cartService.addProductToCart(userId, request.product(), request.quantity())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deleteCart(@PathVariable String userId) {
        log.info("REST: Delete cart - userId: {}", userId);

        return cartService.deleteCart(userId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}