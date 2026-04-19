package dev.makeev.order.client;

public record NotificationResponse(boolean success, String notificationId, String message) {
}
