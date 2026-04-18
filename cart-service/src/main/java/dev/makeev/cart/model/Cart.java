package dev.makeev.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(timeToLive = 3600)
public class Cart {

    private String id;

    private String userId;

    @Indexed
    private Map<String, CartItem> items = new ConcurrentHashMap<>();

    private BigDecimal totalAmount;

    @Transient
    private Instant createdAt;

    @Transient
    private Instant updatedAt;

    public Cart(String userId) {
        this.id = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addItem(CartItem item) {
        items.put(item.getProductId(), item);
        recalculateTotal();
        updatedAt = Instant.now();
    }

    public void removeItem(String productId) {
        items.remove(productId);
        recalculateTotal();
        updatedAt = Instant.now();
    }

    public void updateItem(String productId, CartItem item) {
        items.put(productId, item);
        recalculateTotal();
        updatedAt = Instant.now();
    }

    public void clear() {
        items.clear();
        totalAmount = BigDecimal.ZERO;
        updatedAt = Instant.now();
    }

    private void recalculateTotal() {
        totalAmount = items.values().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
