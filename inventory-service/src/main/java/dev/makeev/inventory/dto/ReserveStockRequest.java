package dev.makeev.inventory.dto;

public record ReserveStockRequest(
        String productId,
        int quantity,
        String orderId) {
}
