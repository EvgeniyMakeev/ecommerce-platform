package dev.makeev.inventory.dto;

public record ReleaseStockRequest(
        String productId,
        int quantity,
        String orderId) {
}
