package dev.makeev.order.client;

public record ConfirmReservationRequest(String productId, int quantity, String orderId) {
}
