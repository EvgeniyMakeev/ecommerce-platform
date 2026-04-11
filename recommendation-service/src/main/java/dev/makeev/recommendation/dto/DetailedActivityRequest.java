package dev.makeev.recommendation.dto;

import dev.makeev.recommendation.model.ActivityType;

public record DetailedActivityRequest(
    String userId,
    String productId,
    String productName,
    String category,
    ActivityType activityType,
    String sessionId,
    Double price,
    Integer quantity
) {}
