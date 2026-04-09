package dev.makeev.inventory.dto;

public record CancelReservationRequest(
        String productId,
        int quantity,
        String orderId) {
}
