package dev.makeev.common.events;

import java.io.Serializable;
import java.time.Instant;

public record ProductEvent(
        String eventId,
        String productId,
        EventType eventType,
        String payload,
        Instant timestamp) implements Serializable {
}
