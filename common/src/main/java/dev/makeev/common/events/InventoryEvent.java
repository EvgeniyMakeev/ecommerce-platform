package dev.makeev.common.events;

import java.time.Instant;

public record InventoryEvent(
        String eventId,
        String productId,
        Integer quantity,
        EventType eventType,
        String orderId,
        Instant timestamp) {
    public enum EventType {
        RESERVED,
        RELEASED,
        UPDATED
    }
}
