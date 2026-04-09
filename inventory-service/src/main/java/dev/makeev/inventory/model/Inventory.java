package dev.makeev.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("inventory")
public class Inventory {

    @Id
    private Long id;

    private String productId;

    private String productName;

    private int quantity;

    private String location;

    private String sku;

    private Instant createdAt;

    private Instant updatedAt;

    // Reservation tracking
    private Map<String, Integer> reservations = new HashMap<>(); // orderId -> quantity

    public Inventory(String productId, String productName, int quantity, String location, String sku) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.location = location;
        this.sku = sku;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
        this.updatedAt = Instant.now();
    }

    public void addStock(int amount) {
        this.quantity += amount;
        this.updatedAt = Instant.now();
    }

    public void removeStock(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
        this.updatedAt = Instant.now();
    }

    public boolean isInStock() {
        return quantity > 0;
    }

    public boolean hasEnoughStock(int requiredQuantity) {
        return quantity >= requiredQuantity;
    }

    public void reserveStock(int quantity, String orderId) {
        if (hasEnoughStock(quantity)) {
            reservations.put(orderId, reservations.getOrDefault(orderId, 0) + quantity);
            updatedAt = Instant.now();
        } else {
            throw new RuntimeException("Insufficient stock for reservation. Available: " + 
                    getAvailableQuantity() + ", Required: " + quantity);
        }
    }

    public void releaseStock(int quantity, String orderId) {
        Integer reserved = reservations.get(orderId);
        if (reserved != null) {
            int releasedAmount = Math.min(quantity, reserved);
            reservations.put(orderId, reserved - releasedAmount);
            if (reservations.get(orderId) == 0) {
                reservations.remove(orderId);
            }
            updatedAt = Instant.now();
        }
    }

    public void confirmReservation(int quantity, String orderId) {
        Integer reserved = reservations.get(orderId);
        if (reserved != null && reserved >= quantity) {
            this.quantity -= quantity;
            reservations.put(orderId, reserved - quantity);
            if (reservations.get(orderId) == 0) {
                reservations.remove(orderId);
            }
            updatedAt = Instant.now();
        }
    }

    public void cancelReservation(int quantity, String orderId) {
        releaseStock(quantity, orderId);
    }

    public int getTotalReserved() {
        return reservations.values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getAvailableQuantity() {
        return quantity - getTotalReserved();
    }

    public boolean hasReservationForOrder(String orderId) {
        return reservations.containsKey(orderId);
    }

    public int getReservedQuantity(String orderId) {
        return reservations.getOrDefault(orderId, 0);
    }

    public boolean hasEnoughAvailableStock(int requiredQuantity) {
        return getAvailableQuantity() >= requiredQuantity;
    }
}
