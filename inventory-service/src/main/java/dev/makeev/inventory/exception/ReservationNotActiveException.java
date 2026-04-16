package dev.makeev.inventory.exception;

public class ReservationNotActiveException extends RuntimeException {
    public ReservationNotActiveException(String orderId) {
        super("Cannot release non-active reservation for order: " + orderId);
    }
}
