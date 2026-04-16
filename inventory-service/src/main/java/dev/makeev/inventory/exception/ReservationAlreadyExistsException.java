package dev.makeev.inventory.exception;

public class ReservationAlreadyExistsException extends RuntimeException {
    public ReservationAlreadyExistsException(String orderId) {
        super("Reservation already exists for order: " + orderId);
    }
}
