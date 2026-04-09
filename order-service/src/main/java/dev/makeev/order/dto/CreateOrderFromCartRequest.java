package dev.makeev.order.dto;

import java.util.List;

public record CreateOrderFromCartRequest(
        String userId,
        List<dev.makeev.common.dto.ProductDTO> cartItems,
        String shippingAddress,
        String billingAddress) {
}
