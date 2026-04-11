package dev.makeev.order.integration;

import dev.makeev.order.model.Order;
import dev.makeev.order.model.OrderItem;
import dev.makeev.order.saga.SagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class, SagaOrchestratorTest.TestSagaConfig.class})
@ActiveProfiles("test")
class SagaOrchestratorTest {

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    @TestConfiguration
    static class TestSagaConfig {
        @Bean
        public SagaOrchestrator sagaOrchestrator(
                dev.makeev.order.service.OrderService orderService,
                dev.makeev.order.client.InventoryServiceClient inventoryServiceClient,
                dev.makeev.order.client.PaymentServiceClient paymentServiceClient,
                dev.makeev.order.client.NotificationServiceClient notificationServiceClient) {
            return new SagaOrchestrator(orderService, inventoryServiceClient, paymentServiceClient, notificationServiceClient);
        }
        
        @Bean
        public dev.makeev.order.service.OrderService orderService() {
            dev.makeev.order.service.OrderService mockService = Mockito.mock(dev.makeev.order.service.OrderService.class);
            Mockito.when(mockService.createOrder(any(String.class), any(List.class), any(String.class), any(String.class)))
                    .thenReturn(Mono.just(new Order()));
            Mockito.when(mockService.updateOrder(any(Long.class), any(Order.class)))
                    .thenReturn(Mono.just(new Order()));
            return mockService;
        }
        
        @Bean
        public dev.makeev.order.repository.OrderRepository orderRepository() {
            return Mockito.mock(dev.makeev.order.repository.OrderRepository.class);
        }
    }

    private Order testOrder;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Saga Orchestrator bean should be properly configured")
    void testSagaOrchestratorBeanExists() {
        assertThat(sagaOrchestrator).isNotNull();
    }

    @Test
    @DisplayName("Order creation should set correct saga state")
    void testOrderCreation() {
        testOrder.startSaga("test-saga-123");
        
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> order.getSagaId() != null &&
                        order.isSagaActive() &&
                       order.getStatus().equals("PROCESSING"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Saga completion should update order status and timestamps")
    void testOrderSagaCompletion() {
        testOrder.startSaga("test-saga-456");
        assertThat(testOrder.isSagaActive()).isTrue();
        
        testOrder.completeSaga();
        
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> !order.isSagaActive() &&
                        !order.hasFailed() &&
                       order.getStatus().equals("CONFIRMED") &&
                       order.getSagaCompletedAt() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Saga failure should set order status to cancelled with failure reason")
    void testOrderSagaFailure() {
        testOrder.startSaga("test-saga-789");
        
        testOrder.failSaga("Test failure reason");
        
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> order.getStatus().equals("CANCELLED") &&
                       order.getFailureReason() != null &&
                       order.getFailureReason().contains("Test failure reason"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Order item calculations should return correct total amount")
    void testOrderItemCalculations() {
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> {
                    BigDecimal expectedTotal = new BigDecimal("250.00");
                    return order.getTotalAmount().equals(expectedTotal) &&
                           order.getItems().size() == 2 &&
                           order.getCurrency().equals("USD");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Order status transitions should work correctly")
    void testOrderStatusTransitions() {
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> order.getStatus().equals("PENDING"))
                .verifyComplete();
        
        testOrder.updateStatus(Order.OrderStatus.CONFIRMED);
        
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> order.getStatus().equals("CONFIRMED"))
                .verifyComplete();
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
        
        StepVerifier.create(Mono.just(testOrder))
                .expectNextMatches(order -> order.getStatus().equals("CONFIRMED") &&
                       order.getSagaCompletedAt() != null)
                .verifyComplete();
    }
}
