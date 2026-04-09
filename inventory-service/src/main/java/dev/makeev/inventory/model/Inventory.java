package dev.makeev.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

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
}
