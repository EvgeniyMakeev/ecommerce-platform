package dev.makeev.order.controller;

import dev.makeev.order.dto.CreateOrderFromCartRequest;
import dev.makeev.order.dto.CreateOrderRequest;
import dev.makeev.order.dto.UpdateStatusRequest;
import dev.makeev.order.model.Order;
import dev.makeev.order.service.OrderService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("REST: Create order request - userId: {}, items: {}",
                request.userId(), request.items().size());

        return orderService.createOrder(request.userId(), request.items(),
                        request.shippingAddress(), request.billingAddress())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{orderNumber}")
    public Mono<ResponseEntity<Order>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        log.info("REST: Get order request - orderNumber: {}", orderNumber);

        return orderService.getOrderByOrderNumber(orderNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    public Flux<Order> getOrdersByUserId(@PathVariable String userId) {
        log.info("REST: Get orders request - userId: {}", userId);

        return orderService.getOrdersByUserId(userId);
    }

    @GetMapping("/status/{status}")
    public Flux<Order> getOrdersByStatus(@PathVariable String status) {
        log.info("REST: Get orders by status - status: {}", status);

        return orderService.getOrdersByStatus(status);
    }

    @PutMapping("/{orderNumber}/status")
    public Mono<ResponseEntity<Order>> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateStatusRequest request) {
        log.info("REST: Update order status - orderNumber: {}, status: {}",
                orderNumber, request.status());

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(request.status().toUpperCase());
        return orderService.updateOrderStatus(orderNumber, newStatus)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{orderNumber}/cancel")
    public Mono<ResponseEntity<Order>> cancelOrder(@PathVariable String orderNumber) {
        log.info("REST: Cancel order request - orderNumber: {}", orderNumber);

        return orderService.cancelOrder(orderNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/{orderNumber}/confirm")
    public Mono<ResponseEntity<Order>> confirmOrder(@PathVariable String orderNumber) {
        log.info("REST: Confirm order request - orderNumber: {}", orderNumber);

        return orderService.confirmOrder(orderNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/from-cart")
    public Mono<ResponseEntity<Order>> createOrderFromCart(@Valid @RequestBody CreateOrderFromCartRequest request) {
        log.info("REST: Create order from cart request - userId: {}, items: {}",
                request.userId(), request.cartItems().size());

        return orderService.createOrderFromCart(request.userId(), request.cartItems(),
                        request.shippingAddress(), request.billingAddress())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/users/{userId}/orders/{orderNumber}")
    public Mono<ResponseEntity<Order>> getUserOrder(@PathVariable String userId, @PathVariable String orderNumber) {
        log.info("REST: Get user order - userId: {}, orderNumber: {}", userId, orderNumber);

        return orderService.getUserOrder(userId, orderNumber)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderNumber}")
    public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable String orderNumber) {
        log.info("REST: Delete order request - orderNumber: {}", orderNumber);

        return orderService.deleteOrder(orderNumber)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}