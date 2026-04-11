package dev.makeev.recommendation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record RatingRequest(
        @NotNull(message = "Rating is required")
        @Min(value = 0, message = "Rating cannot be less than 0")
        @Max(value = 5, message = "Rating cannot be greater than 5")
        Double rating
) {}
