package dev.makeev.order.dto;

public record OrderStatisticsResponse(
        long pendingOrders,
        long confirmedOrders,
        long cancelledOrders
) {}
