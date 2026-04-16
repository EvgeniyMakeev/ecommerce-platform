package dev.makeev.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId, int available, int required) {
        super("Insufficient stock for product: " + productId + ". Available: " + available + ", Required: " + required);
    }
}
