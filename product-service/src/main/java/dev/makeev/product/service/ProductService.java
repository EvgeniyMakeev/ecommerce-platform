package dev.makeev.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.makeev.common.dto.ProductDTO;
import dev.makeev.common.events.ProductEvent;
import dev.makeev.common.events.EventType;
import dev.makeev.product.model.Product;
import dev.makeev.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import dev.makeev.common.config.ProductBindings;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductServiceInterface {

    private final ProductRepository productRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<ProductDTO> createProduct(ProductDTO productDTO) {
        log.info("Creating product: {}", productDTO.name());

        return Mono.fromCallable(() -> Product.builder()
                .name(productDTO.name())
                .name(productDTO.name())
                .price(productDTO.price())
                .category(productDTO.category())
                .tags(serializeTags(productDTO.tags()))
                .imageUrl(productDTO.imageUrl())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build())
                .flatMap(productRepository::save)
                .flatMap(product -> {
                    publishProductEvent(product, EventType.CREATED);
                    return Mono.just(toDTO(product));
                })
                .doOnSuccess(dto -> log.info("Product created with ID: {}", dto.id()))
                .doOnError(error -> log.error("Error creating product", error));
    }

    @Override
    public Mono<ProductDTO> updateProduct(String id, ProductDTO productDTO) {
        log.info("Updating product: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + id)))
                .flatMap(existing -> {
                    existing.setName(productDTO.name());
                    existing.setDescription(productDTO.description());
                    existing.setPrice(productDTO.price());
                    existing.setCategory(productDTO.category());
                    existing.setImageUrl(productDTO.imageUrl());
                    existing.setUpdatedAt(Instant.now());

                    try {
                        existing.setTags(serializeTags(productDTO.tags()));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }

                    return productRepository.save(existing);
                })
                .flatMap(product -> {
                    publishProductEvent(product, EventType.UPDATED);
                    return Mono.just(toDTO(product));
                })
                .doOnSuccess(dto -> log.info("Product updated: {}", id))
                .doOnError(error -> log.error("Error updating product: {}", id, error));
    }

    @Override
    public Mono<Void> deleteProduct(String id) {
        log.info("Deleting product: {}", id);

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + id)))
                .flatMap(product -> {
                    publishProductEvent(product, EventType.DELETED);
                    return productRepository.deleteById(id);
                })
                .doOnSuccess(v -> log.info("Product deleted: {}", id))
                .doOnError(error -> log.error("Error deleting product: {}", id, error));
    }

    @Override
    public Mono<ProductDTO> getProduct(String id) {
        log.debug("Fetching product: {}", id);

        return productRepository.findById(id)
                .map(this::toDTO)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + id)));
    }

    @Override
    public Flux<ProductDTO> getAllProducts() {
        log.debug("Fetching all products");

        return productRepository.findAll()
                .map(this::toDTO);
    }

    @Override
    public Flux<ProductDTO> getProductsByCategory(String category) {
        log.debug("Fetching products by category: {}", category);

        return productRepository.findByCategory(category)
                .map(this::toDTO);
    }

    @Override
    public Flux<ProductDTO> searchProducts(String searchTerm) {
        log.debug("Searching products: {}", searchTerm);

        return productRepository.searchByName(searchTerm)
                .map(this::toDTO);
    }

    private void publishProductEvent(Product product, EventType eventType) {
        try {
            var productDTO = toDTO(product);
            var payload = objectMapper.writeValueAsString(productDTO);
            var event = new ProductEvent(
                    UUID.randomUUID().toString(),
                    product.getId(),
                    eventType,
                    payload,
                    Instant.now());

            streamBridge.send(ProductBindings.PRODUCT_EVENTS_OUTPUT.getBindingName(), event);

            log.info("Published {} event for product: {}", eventType, product.getId());
        } catch (JsonProcessingException e) {
            log.error("Error publishing product event", e);
        }
    }

    private ProductDTO toDTO(Product product) {
        try {
            var tags = deserializeTags(product.getTags());
            return new ProductDTO(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getCategory(),
                    tags,
                    product.getImageUrl(),
                    product.getCreatedAt(),
                    product.getUpdatedAt());
        } catch (JsonProcessingException e) {
            log.error("Error converting Product to DTO", e);
            throw new RuntimeException("Error converting product", e);
        }
    }

    private String serializeTags(List<String> tags) throws JsonProcessingException {
        if (tags == null || tags.isEmpty()) {
            return "[]";
        }
        return objectMapper.writeValueAsString(tags);
    }

    private List<String> deserializeTags(String tagsJson) throws JsonProcessingException {
        if (tagsJson == null || tagsJson.isEmpty() || tagsJson.equals("[]")) {
            return Collections.emptyList();
        }
        return Arrays.asList(objectMapper.readValue(tagsJson, String[].class));
    }
}