package dev.makeev.search.listener;

import dev.makeev.common.events.ProductEvent;
import dev.makeev.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final SearchService searchService;

    public void handleProductEvent(ProductEvent event) {
        log.info("Processing product event: {} for product: {}", event.eventType(), event.productId());
        
        switch (event.eventType()) {
            case CREATED, UPDATED -> // TODO: Parse payload and call searchService.indexProduct(productDTO)
                    log.info("Product {} event received for product: {}", event.eventType(), event.productId());
            case DELETED -> searchService.deleteProduct(event.productId())
                    .doOnSuccess(v -> log.info("Successfully deleted product from search: {}", event.productId()))
                    .doOnError(error -> log.error("Error deleting product from search: {}", event.productId(), error))
                    .subscribe();
        }
    }
}
