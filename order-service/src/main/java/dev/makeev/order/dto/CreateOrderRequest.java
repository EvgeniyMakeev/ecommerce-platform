package dev.makeev.order.dto;

import java.util.List;

public record CreateOrderRequest(
        String userId,
        List<dev.makeev.order.model.OrderItem> items,
        String shippingAddress,
        String billingAddress) {
}
