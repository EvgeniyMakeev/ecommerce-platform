package dev.makeev.order.dto;

public record PaymentResponse(boolean success, String transactionId, String errorMessage) {
}
