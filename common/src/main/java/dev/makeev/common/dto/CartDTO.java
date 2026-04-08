package dev.makeev.common.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record CartDTO(
        String userId,
        List<CartItemDTO> items,
        BigDecimal totalAmount) {
    public CartDTO {
        if (items == null) {
            items = new ArrayList<>();
        }
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
    }

    public CartDTO(String userId) {
        this(userId, new ArrayList<>(), BigDecimal.ZERO);
    }
}
