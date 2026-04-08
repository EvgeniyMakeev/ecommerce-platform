package dev.makeev.product.dto;

/**
 * Error response DTO for API error responses
 */
public record ErrorResponse(int status, String message, long timestamp) {}
