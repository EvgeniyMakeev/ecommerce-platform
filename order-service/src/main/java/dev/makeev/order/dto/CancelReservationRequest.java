package dev.makeev.order.dto;

public record CancelReservationRequest(String productId, int quantity, String orderId) {
}
