package dev.makeev.order.dto;

import java.time.Instant;

public record SagaStatusResponse(
        String sagaId,
        boolean isActive,
        boolean hasFailed,
        String failureReason,
        Instant startedAt,
        Instant completedAt
) {}
