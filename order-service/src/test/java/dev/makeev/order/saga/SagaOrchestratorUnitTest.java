package dev.makeev.order.saga;

import dev.makeev.order.dto.InventoryResponse;
import dev.makeev.order.client.InventoryServiceClient;
import dev.makeev.order.client.NotificationServiceClient;
import dev.makeev.order.client.PaymentServiceClient;
import dev.makeev.order.model.Order;
import dev.makeev.order.model.OrderItem;
import dev.makeev.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Saga Orchestrator Unit Tests")
class SagaOrchestratorUnitTest {

    @Mock
    private OrderService orderService;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    private PaymentServiceClient paymentServiceClient;

    private NotificationServiceClient notificationServiceClient;

    private SagaOrchestrator sagaOrchestrator;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        paymentServiceClient = new PaymentServiceClient();
        notificationServiceClient = new NotificationServiceClient();
        sagaOrchestrator = new SagaOrchestrator(orderService, inventoryServiceClient,
                paymentServiceClient, notificationServiceClient);

        List<OrderItem> testItems = List.of(
                new OrderItem(null, null, "product-1", "Test Product 1", 
                        new BigDecimal("100.00"), 2, "http://example.com/image1.jpg"),
                new OrderItem(null, null, "product-2", "Test Product 2", 
                        new BigDecimal("50.00"), 1, "http://example.com/image2.jpg")
        );

        testOrder = new Order(
                "test-user-123",
                testItems,
                "123 Test Street, Test City, TC 12345",
                "123 Test Street, Test City, TC 12345"
        );
        testOrder.setId(1L);
    }

    @Test
    @DisplayName("Saga Orchestrator should be properly initialized")
    void testSagaOrchestratorInitialization() {
        assertThat(sagaOrchestrator).isNotNull();
    }

    @Test
    @DisplayName("Order creation should set correct saga state")
    void testOrderCreation() {
        testOrder.startSaga("test-saga-123");
        
        assertThat(testOrder.getSagaId()).isNotNull();
        assertThat(testOrder.isSagaActive()).isTrue();
        assertThat(testOrder.getStatus()).isEqualTo("PROCESSING");
    }

    @Test
    @DisplayName("Saga completion should update order status")
    void testOrderSagaCompletion() {
        testOrder.startSaga("test-saga-456");
        assertThat(testOrder.isSagaActive()).isTrue();
        
        testOrder.completeSaga();
        
        assertThat(testOrder.isSagaActive()).isFalse();
        assertThat(testOrder.hasFailed()).isFalse();
        assertThat(testOrder.getStatus()).isEqualTo("CONFIRMED");
        assertThat(testOrder.getSagaCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Saga failure should set order status to cancelled")
    void testOrderSagaFailure() {
        testOrder.startSaga("test-saga-789");
        
        testOrder.failSaga("Test failure reason");
        
        assertThat(testOrder.getStatus()).isEqualTo("CANCELLED");
        assertThat(testOrder.getFailureReason()).isNotNull();
        assertThat(testOrder.getFailureReason()).contains("Test failure reason");
    }

    @Test
    @DisplayName("Order item calculations should return correct total amount")
    void testOrderItemCalculations() {
        BigDecimal expectedTotal = new BigDecimal("250.00");
        assertThat(testOrder.getTotalAmount()).isEqualTo(expectedTotal);
        assertThat(testOrder.getItems()).hasSize(2);
        assertThat(testOrder.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Order status transitions should work correctly")
    void testOrderStatusTransitions() {
        assertThat(testOrder.getStatus()).isEqualTo("PENDING");
        
        testOrder.updateStatus(Order.OrderStatus.CONFIRMED);
        
        assertThat(testOrder.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Saga state transitions should work correctly through lifecycle")
    void testSagaStateTransitions() {
        assertThat(testOrder.getSagaId()).isNull();
        assertThat(testOrder.isSagaActive()).isFalse();
        
        testOrder.startSaga("saga-test");
        assertThat(testOrder.getSagaId()).isNotNull();
        assertThat(testOrder.isSagaActive()).isTrue();
        
        testOrder.completeSaga();
        assertThat(testOrder.isSagaActive()).isFalse();
        assertThat(testOrder.hasFailed()).isFalse();
        assertThat(testOrder.getStatus()).isEqualTo("CONFIRMED");
        assertThat(testOrder.getSagaCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("Successful saga execution should complete all steps")
    void testSuccessfulSagaExecution() {
        when(orderService.updateOrder(any(Long.class), any(Order.class)))
                .thenReturn(Mono.just(testOrder));
        when(inventoryServiceClient.reserveInventory(any(Order.class)))
                .thenReturn(Mono.just(new InventoryResponse(true, "Success")));
        when(inventoryServiceClient.confirmInventoryReservation(any(Order.class)))
                .thenReturn(Mono.just(new InventoryResponse(true, "Success")));

        StepVerifier.create(sagaOrchestrator.executeOrderSaga(testOrder))
                .expectNextMatches(order ->
                        order.getStatus().equals("CONFIRMED") &&
                        !order.isSagaActive() &&
                        order.getSagaCompletedAt() != null)
                .verifyComplete();

        verify(inventoryServiceClient).reserveInventory(testOrder);
        verify(inventoryServiceClient).confirmInventoryReservation(testOrder);
        verify(orderService, times(4)).updateOrder(any(Long.class), any(Order.class)); // Initial + 3 updates
    }

    @Test
    @DisplayName("Failed saga execution should trigger compensation")
    void testFailedSagaExecution() {
        when(orderService.updateOrder(any(Long.class), any(Order.class)))
                .thenReturn(Mono.just(testOrder));
        when(inventoryServiceClient.reserveInventory(any(Order.class)))
                .thenReturn(Mono.error(new RuntimeException("Inventory unavailable")));
        when(inventoryServiceClient.releaseInventoryReservation(any(Order.class)))
                .thenReturn(Mono.just(new InventoryResponse(true, "Released")));

        StepVerifier.create(sagaOrchestrator.executeOrderSaga(testOrder))
                .expectNextMatches(order ->
                        order.getStatus().equals("CANCELLED") &&
                        !order.isSagaActive() &&
                        order.getFailureReason() != null)
                .verifyComplete();

        verify(inventoryServiceClient, times(2)).releaseInventoryReservation(testOrder);
    }
}
