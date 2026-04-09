package dev.makeev.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class Order {

    @Id
    private Long id;

    private String orderNumber;

    private String userId;

    private String status;

    private BigDecimal totalAmount;

    private String currency;

    private String shippingAddress;

    private String billingAddress;

    private List<OrderItem> items;

    private Instant createdAt;

    private Instant updatedAt;

    public Order(String userId, List<OrderItem> items, String shippingAddress, String billingAddress) {
        this.orderNumber = UUID.randomUUID().toString();
        this.userId = userId;
        this.status = OrderStatus.PENDING.name();
        this.totalAmount = calculateTotal(items);
        this.currency = "USD";
        this.shippingAddress = shippingAddress;
        this.billingAddress = billingAddress;
        this.items = items;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus.name();
        this.updatedAt = Instant.now();
    }

    public void updateTotalAmount() {
        this.totalAmount = calculateTotal(items);
        this.updatedAt = Instant.now();
    }

    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    }
}
