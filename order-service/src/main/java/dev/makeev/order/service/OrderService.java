package dev.makeev.order.service;

import dev.makeev.common.dto.ProductDTO;
import dev.makeev.order.model.Order;
import dev.makeev.order.model.OrderItem;
import dev.makeev.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Mono<Order> createOrder(String userId, List<OrderItem> items, String shippingAddress, String billingAddress) {
        log.info("Creating order for user: {} with {} items", userId, items.size());
        
        Order order = new Order(userId, items, shippingAddress, billingAddress);
        return orderRepository.save(order)
                .doOnSuccess(savedOrder -> log.info("Created order: {} for user: {}",
                        savedOrder.getOrderNumber(), userId))
                .doOnError(error -> log.error("Error creating order for user {}: {}", userId, error.toString()));
    }

    public Mono<Order> getOrderByOrderNumber(String orderNumber) {
        log.info("Getting order by order number: {}", orderNumber);
        return orderRepository.findByOrderNumber(orderNumber)
                .doOnSuccess(order -> log.debug("Found order: {} for user: {}",
                        order.getOrderNumber(), order.getUserId()))
                .doOnError(error -> log.error("Error getting order {}: {}", orderNumber, error.toString()));
    }

    public Flux<Order> getOrdersByUserId(String userId) {
        log.info("Getting orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .doOnComplete(() -> log.debug("Completed fetching orders for user: {}", userId))
                .doOnError(error -> log.error("Error getting orders for user {}: {}", userId, error.toString()));
    }

    public Flux<Order> getOrdersByStatus(String status) {
        log.info("Getting orders by status: {}", status);
        return orderRepository.findByStatus(status)
                .doOnComplete(() -> log.debug("Completed fetching orders with status: {}", status))
                .doOnError(error -> log.error("Error getting orders by status {}: {}", status, error.toString()));
    }

    public Mono<Order> updateOrderStatus(String orderNumber, Order.OrderStatus newStatus) {
        log.info("Updating order {} status to: {}", orderNumber, newStatus);
        
        return orderRepository.findByOrderNumber(orderNumber)
                .flatMap(order -> {
                    order.updateStatus(newStatus);
                    return orderRepository.save(order);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + orderNumber)))
                .doOnSuccess(updated -> log.info("Updated order {} status to: {}", 
                        orderNumber, newStatus))
                .doOnError(error -> log.error("Error updating order {} status: {}", orderNumber, error.toString()));
    }

    public Mono<Order> createOrderFromCart(String userId, List<ProductDTO> cartItems, String shippingAddress, String billingAddress) {
        log.info("Creating order from cart for user: {} with {} products", userId, cartItems.size());
        
        List<OrderItem> orderItems = cartItems.stream()
                .map(product -> new OrderItem(
                        null,
                        product.id(),
                        product.name(),
                        product.price(),
                        1,
                        product.imageUrl()
                ))
                .toList();
        
        Order order = new Order(userId, orderItems, shippingAddress, billingAddress);
        return orderRepository.save(order)
                .doOnSuccess(savedOrder -> log.info("Created order from cart: {} for user: {}", 
                        savedOrder.getOrderNumber(), userId))
                .doOnError(error -> log.error("Error creating order from cart for user {}: {}", userId, error.toString()));
    }

    public Mono<Order> getUserOrder(String userId, String orderNumber) {
        log.info("Getting order {} for user: {}", orderNumber, userId);
        return orderRepository.findByUserIdAndOrderNumber(userId, orderNumber)
                .doOnSuccess(order -> log.debug("Found order: {} for user: {}", 
                        order.getOrderNumber(), userId))
                .doOnError(error -> log.error("Error getting order {} for user {}: {}", orderNumber, userId, error.toString()));
    }

    public Mono<Order> cancelOrder(String orderNumber) {
        log.info("Cancelling order: {}", orderNumber);
        
        return updateOrderStatus(orderNumber, Order.OrderStatus.CANCELLED);
    }

    public Mono<Order> confirmOrder(String orderNumber) {
        log.info("Confirming order: {}", orderNumber);
        
        return updateOrderStatus(orderNumber, Order.OrderStatus.CONFIRMED);
    }

    public Mono<Void> deleteOrder(String orderNumber) {
        log.info("Deleting order: {}", orderNumber);
        
        return orderRepository.findByOrderNumber(orderNumber)
                .flatMap(orderRepository::delete)
                .then()
                .doOnSuccess(v -> log.info("Deleted order: {}", orderNumber))
                .doOnError(error -> log.error("Error deleting order {}: {}", orderNumber, error.toString()));
    }
}
