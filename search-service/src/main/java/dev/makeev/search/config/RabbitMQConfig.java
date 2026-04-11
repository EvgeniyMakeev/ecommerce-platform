package dev.makeev.search.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.makeev.common.dto.ProductDTO;
import dev.makeev.common.events.ProductEvent;
import dev.makeev.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    @Bean
    public Function<Flux<Message<ProductEvent>>, Flux<Message<String>>> productEventsProcessor() {
        return flux -> flux
                .concatMap(this::processProductEvent)
                .onErrorContinue((error, event) -> 
                    log.error("Error processing product event: {}", event, error));
    }

    private Mono<Message<String>> processProductEvent(Message<ProductEvent> message) {
        ProductEvent event = message.getPayload();
        log.info("Processing product event: {} for product: {}", event.eventType(), event.productId());
        
        return switch (event.eventType()) {
            case CREATED, UPDATED -> handleCreateOrUpdateEvent(event);
            case DELETED -> handleDeleteEvent(event);
        };
    }

    private Mono<Message<String>> handleCreateOrUpdateEvent(ProductEvent event) {
        try {
            ProductDTO productDTO = objectMapper.readValue(event.payload(), ProductDTO.class);
            log.info("Parsed product DTO for product: {}", productDTO.id());
            
            return searchService.indexProduct(productDTO)
                    .doOnSuccess(dto -> log.info("Successfully indexed product: {}", dto.id()))
                    .doOnError(error -> log.error("Error indexing product {}: {}", productDTO.id(), error.toString()))
                    .map(dto -> MessageBuilder.withPayload("Indexed product " + dto.id() + " in search").build());
                    
        } catch (Exception e) {
            log.error("Error parsing product event payload for product {}: {}", 
                    event.productId(), e.getMessage(), e);
            return Mono.just(MessageBuilder.withPayload("Failed to parse product " + event.productId() + ": " + e.getMessage()).build());
        }
    }

    private Mono<Message<String>> handleDeleteEvent(ProductEvent event) {
        return searchService.deleteProduct(event.productId())
                .doOnSuccess(v -> log.info("Successfully deleted product from search: {}", event.productId()))
                .doOnError(error -> log.error("Error deleting product from search: {}", event.productId(), error))
                .map(v -> MessageBuilder.withPayload("Deleted product " + event.productId() + " from search").build());
    }
}
