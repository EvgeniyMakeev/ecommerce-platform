package dev.makeev.inventory.dto;

public record ConfirmReservationRequest(
        String productId,
        int quantity,
        String orderId) {
}
