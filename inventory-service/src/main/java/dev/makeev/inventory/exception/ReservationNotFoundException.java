package dev.makeev.inventory.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(String orderId) {
        super("Reservation not found for order: " + orderId);
    }
}
