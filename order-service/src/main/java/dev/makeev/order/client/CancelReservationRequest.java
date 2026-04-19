package dev.makeev.order.client;

public record CancelReservationRequest(String productId, int quantity, String orderId) {
}
