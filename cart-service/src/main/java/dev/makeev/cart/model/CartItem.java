package dev.makeev.cart.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(timeToLive = 3600)
public class CartItem {

    @Id
    private String id;

    private String productId;

    private String productName;

    private BigDecimal price;

    private int quantity;

    private String imageUrl;

    public CartItem(String productId, String productName, BigDecimal price, int quantity, String imageUrl) {
        this.id = java.util.UUID.randomUUID().toString();
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
