package dev.makeev.common.events;

import java.time.Instant;

public record OrderEvent(
        String eventId,
        String orderId,
        EventType eventType,
        String payload,
        Instant timestamp) {
    public enum EventType {
        CREATED,
        CONFIRMED,
        CANCELLED,
        PAYMENT_COMPLETED
    }
}
