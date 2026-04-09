package dev.makeev.search.repository;

import dev.makeev.search.model.ProductDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class SearchRepository {

    private final ConcurrentHashMap<String, ProductDocument> inMemoryStore = new ConcurrentHashMap<>();

    public Flux<ProductDocument> findByNameContainingIgnoreCase(String name) {
        return Flux.fromIterable(inMemoryStore.values())
                .filter(product -> product.getName().toLowerCase().contains(name.toLowerCase()));
    }
    
    public Flux<ProductDocument> findByCategoryIgnoreCase(String category) {
        return Flux.fromIterable(inMemoryStore.values())
                .filter(product -> product.getCategory().equalsIgnoreCase(category));
    }
    
    public Flux<ProductDocument> findByTagsContaining(String tag) {
        return Flux.fromIterable(inMemoryStore.values())
                .filter(product -> product.getTags().contains(tag));
    }
    
    public Flux<ProductDocument> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return Flux.fromIterable(inMemoryStore.values())
                .filter(product -> product.getPrice().compareTo(minPrice) >= 0 && 
                                 product.getPrice().compareTo(maxPrice) <= 0);
    }
    
    public Flux<ProductDocument> findByNameContainingIgnoreCaseAndCategoryIgnoreCase(String name, String category) {
        return findByNameContainingIgnoreCase(name)
                .filter(product -> product.getCategory().equalsIgnoreCase(category));
    }
    
    public Flux<ProductDocument> findByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name) {
        return findByNameContainingIgnoreCase(name)
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
    }
    
    public Flux<ProductDocument> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category) {
        return findByCategoryIgnoreCase(category)
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
    }
    
    public Flux<ProductDocument> findByTagsContainingOrderByCreatedAtDesc(String tag) {
        return findByTagsContaining(tag)
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
    }
    
    public Flux<ProductDocument> findByPriceBetweenOrderByPriceAsc(BigDecimal minPrice, BigDecimal maxPrice) {
        return findByPriceBetween(minPrice, maxPrice)
                .sort(Comparator.comparing(ProductDocument::getPrice));
    }
    
    public Flux<ProductDocument> searchByText(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        
        String searchTerm = query.toLowerCase().trim();
        return Flux.fromIterable(inMemoryStore.values())
                .filter(product -> 
                    product.getName().toLowerCase().contains(searchTerm) ||
                    product.getCategory().toLowerCase().contains(searchTerm) ||
                    product.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(searchTerm)))
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
    }
    
    public Flux<ProductDocument> searchWithFilters(String query, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        Flux<ProductDocument> baseSearch = query != null && !query.trim().isEmpty() 
                ? searchByText(query) 
                : findAll();
        
        if (category != null && !category.trim().isEmpty()) {
            baseSearch = baseSearch.filter(product -> 
                    product.getCategory().equalsIgnoreCase(category.trim()));
        }
        
        if (minPrice != null) {
            baseSearch = baseSearch.filter(product -> 
                    product.getPrice().compareTo(minPrice) >= 0);
        }
        
        if (maxPrice != null) {
            baseSearch = baseSearch.filter(product -> 
                    product.getPrice().compareTo(maxPrice) <= 0);
        }
        
        return baseSearch;
    }
    
    public Mono<Long> count() {
        return Mono.just((long) inMemoryStore.size());
    }
    
    public Mono<Long> countByCategoryIgnoreCase(String category) {
        return Mono.just(inMemoryStore.values().stream()
                .filter(product -> product.getCategory().equalsIgnoreCase(category))
                .count());
    }
    
    public Flux<ProductDocument> findTop10ByNameContainingIgnoreCaseOrderByCreatedAtDesc(String name) {
        return findByNameContainingIgnoreCaseOrderByCreatedAtDesc(name)
                .take(10);
    }
    
    public Flux<ProductDocument> findTop10ByCategoryIgnoreCaseOrderByCreatedAtDesc(String category) {
        return findByCategoryIgnoreCaseOrderByCreatedAtDesc(category)
                .take(10);
    }
    
    public Flux<ProductDocument> findTop10ByTagsContainingOrderByCreatedAtDesc(String tag) {
        return findByTagsContainingOrderByCreatedAtDesc(tag)
                .take(10);
    }
    
    public Flux<ProductDocument> findTop10ByOrderByCreatedAtDesc() {
        return findAll()
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .take(10);
    }
    
    public Flux<ProductDocument> findTop10ByPriceBetweenOrderByPriceAsc(BigDecimal minPrice, BigDecimal maxPrice) {
        return findByPriceBetweenOrderByPriceAsc(minPrice, maxPrice)
                .take(10);
    }

    public Mono<ProductDocument> save(ProductDocument entity) {
        return Mono.fromCallable(() -> {
            inMemoryStore.put(entity.getId(), entity);
            return entity;
        });
    }

    public Mono<ProductDocument> findById(String id) {
        return Mono.justOrEmpty(inMemoryStore.get(id));
    }

    public boolean existsById(String id) {
        return inMemoryStore.containsKey(id);
    }

    public Flux<ProductDocument> findAll() {
        return Flux.fromIterable(new ArrayList<>(inMemoryStore.values()))
                .sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
    }

    public Flux<ProductDocument> findAllById(Iterable<String> ids) {
        return Flux.fromIterable(ids)
                .map(inMemoryStore::get)
                .filter(java.util.Objects::nonNull);
    }

    public Mono<Void> deleteById(String id) {
        return Mono.fromRunnable(() -> inMemoryStore.remove(id));
    }

    public Mono<Void> delete(ProductDocument entity) {
        return deleteById(entity.getId());
    }

    public Mono<Void> deleteAllById(Iterable<? extends String> ids) {
        return Flux.fromIterable(ids)
                .flatMap(this::deleteById)
                .then();
    }

    public Mono<Void> deleteAll(Iterable<? extends ProductDocument> entities) {
        return Flux.fromIterable(entities)
                .flatMap(this::delete)
                .then();
    }

    public Mono<Void> deleteAll() {
        return Mono.fromRunnable(inMemoryStore::clear);
    }
}
