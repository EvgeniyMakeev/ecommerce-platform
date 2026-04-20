package dev.makeev.order.dto;

public record ReserveRequest(String productId, int quantity, String orderId) {
}
