package dev.makeev.cart.dto;

import dev.makeev.common.dto.ProductDTO;

public record AddProductRequest(
        ProductDTO product,
        int quantity) {
}
