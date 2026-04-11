package dev.makeev.recommendation.dto;

import dev.makeev.recommendation.model.ActivityType;

public record ActivityRequest(
    String userId,
    String productId,
    String productName,
    String category,
    ActivityType activityType,
    String sessionId
) {}
