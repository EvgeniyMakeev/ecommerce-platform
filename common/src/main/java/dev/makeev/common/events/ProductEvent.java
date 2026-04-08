package dev.makeev.common.events;

import java.time.Instant;

public record ProductEvent(
        String eventId,
        String productId,
        EventType eventType,
        String payload,
        Instant timestamp) {
    public enum EventType {
        CREATED,
        UPDATED,
        DELETED
    }
}
