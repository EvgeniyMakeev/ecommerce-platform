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

    private String sagaId;
    private String compensationData;
    private String failureReason;
    private Instant sagaStartedAt;
    private Instant sagaCompletedAt;

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

    public void startSaga(String sagaId) {
        this.sagaId = sagaId;
        this.sagaStartedAt = Instant.now();
        this.status = OrderStatus.PROCESSING.name();
        this.updatedAt = Instant.now();
    }

    public void completeSaga() {
        this.sagaCompletedAt = Instant.now();
        this.status = OrderStatus.CONFIRMED.name();
        this.updatedAt = Instant.now();
    }

    public void failSaga(String failureReason) {
        this.failureReason = failureReason;
        this.status = OrderStatus.CANCELLED.name();
        this.sagaCompletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void setCompensationData(String compensationData) {
        this.compensationData = compensationData;
        this.updatedAt = Instant.now();
    }

    public boolean isSagaActive() {
        return sagaStartedAt != null && sagaCompletedAt == null;
    }

    public boolean hasFailed() {
        return failureReason != null && !failureReason.trim().isEmpty();
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
