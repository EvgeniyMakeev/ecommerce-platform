package dev.makeev.order.dto;

public record ConfirmReservationRequest(String productId, int quantity, String orderId) {
}
