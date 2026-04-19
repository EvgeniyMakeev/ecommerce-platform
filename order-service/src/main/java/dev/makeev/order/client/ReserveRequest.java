package dev.makeev.order.client;

public record ReserveRequest(String productId, int quantity, String orderId) {
}
