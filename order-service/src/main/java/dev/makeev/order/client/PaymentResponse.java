package dev.makeev.order.client;

public record PaymentResponse(boolean success, String transactionId, String errorMessage) {
}
