package dev.makeev.inventory.config;

import dev.makeev.common.events.ProductEvent;
import dev.makeev.inventory.listener.ProductEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductEventConfig {

    private final ProductEventListener productEventListener;

    @Bean
    public Consumer<Flux<Message<ProductEvent>>> productEventsProcessor() {
        return flux -> flux
                .concatMap(message -> {
                    ProductEvent event = message.getPayload();
                    log.info("Processing product event: {} for product: {}", event.eventType(), event.productId());
                    return productEventListener.handleProductEventAsync(event)
                            .onErrorResume(error -> {
                                log.error("Error processing product event for product {}: {}", 
                                        event.productId(), error.getMessage(), error);
                                return reactor.core.publisher.Mono.empty();
                            });
                })
                .onErrorContinue((error, event) -> 
                    log.error("Error processing product event: {}", event, error));
    }
}
