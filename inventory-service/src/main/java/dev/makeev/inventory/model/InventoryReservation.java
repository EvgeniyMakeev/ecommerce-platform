package dev.makeev.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("inventory_reservations")
public class InventoryReservation {

    @Id
    private Long id;

    private String productId;

    private String orderId;

    private int quantity;

    private ReservationStatus status;

    private Instant createdAt;

    private Instant updatedAt;

    public InventoryReservation(String productId, String orderId, int quantity, ReservationStatus status) {
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public boolean isActive() {
        return ReservationStatus.PENDING.equals(status);
    }
}
