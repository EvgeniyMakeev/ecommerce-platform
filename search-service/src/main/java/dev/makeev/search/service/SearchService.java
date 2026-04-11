package dev.makeev.search.service;

import dev.makeev.common.dto.ProductDTO;
import dev.makeev.search.model.ProductDocument;
import dev.makeev.search.repository.ElasticsearchSearchRepository;
import dev.makeev.search.repository.ElasticsearchSearchRepositoryImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchSearchRepository elasticsearchSearchRepository;
    private final ElasticsearchSearchRepositoryImpl searchRepositoryImpl;

    public Mono<ProductDTO> indexProduct(ProductDTO productDTO) {
        log.info("Indexing product: {}", productDTO.id());
        ProductDocument document = ProductDocument.from(productDTO);
        return elasticsearchSearchRepository.save(document)
                .map(ProductDocument::toDTO)
                .doOnSuccess(dto -> log.info("Successfully indexed product: {}", dto.id()))
                .doOnError(error -> log.error("Error indexing product {}: {}", productDTO.id(), error.toString()));
    }


    public Mono<Void> deleteProduct(String productId) {
        log.info("Deleting product from search: {}", productId);
        return elasticsearchSearchRepository.deleteById(productId)
                .doOnSuccess(v -> log.info("Successfully deleted product from search: {}", productId))
                .doOnError(error -> log.error("Error deleting product from search {}: {}", productId, error.toString()));
    }

    public Flux<ProductDTO> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        return searchRepositoryImpl.searchByText(query)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed search for query: {}", query))
                .doOnError(error -> log.error("Error searching products with query {}: {}", query, error.toString()));
    }

    public Flux<ProductDTO> searchByName(String name) {
        log.info("Searching products by name: {}", name);
        return searchRepositoryImpl.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(name)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed search by name: {}", name))
                .doOnError(error -> log.error("Error searching products by name {}: {}", name, error.toString()));
    }

    public Flux<ProductDTO> searchByCategory(String category) {
        log.info("Searching products by category: {}", category);
        return searchRepositoryImpl.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed search by category: {}", category))
                .doOnError(error -> log.error("Error searching products by category {}: {}", category, error.toString()));
    }

    public Flux<ProductDTO> searchByTags(String tag) {
        log.info("Searching products by tag: {}", tag);
        return searchRepositoryImpl.findByTagsContainingOrderByCreatedAtDesc(tag)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed search by tag: {}", tag))
                .doOnError(error -> log.error("Error searching products by tag {}: {}", tag, error.toString()));
    }

    public Flux<ProductDTO> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Searching products by price range: {} - {}", minPrice, maxPrice);
        return searchRepositoryImpl.findByPriceBetweenOrderByPriceAsc(minPrice, maxPrice)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed search by price range: {} - {}", minPrice, maxPrice))
                .doOnError(error -> log.error("Error searching products by price range {} - {} : {}", minPrice, maxPrice, error.toString()));
    }

    public Flux<ProductDTO> searchWithFilters(String query, String category, String tags, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Advanced search - query: {}, category: {}, tags: {}, price: {} - {}", query, category, tags, minPrice, maxPrice);
        return searchRepositoryImpl.searchWithFilters(query, category, tags, minPrice, maxPrice)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed advanced search"))
                .doOnError(error -> log.error("Error in advanced search: {}", error.toString()));
    }

    public Flux<ProductDTO> getProductSuggestions(String query) {
        log.info("Getting product suggestions for: {}", query);
        return searchRepositoryImpl.findTop10ByNameContainingIgnoreCaseOrderByCreatedAtDesc(query)
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed product suggestions for: {}", query))
                .doOnError(error -> log.error("Error getting product suggestions for {}: {}", query, error.toString()));
    }

    public Flux<ProductDTO> getRecentProducts() {
        log.info("Getting recent products");
        return searchRepositoryImpl.findTop10ByOrderByCreatedAtDesc()
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed getting recent products"))
                .doOnError(error -> log.error("Error getting recent products: {}", error.toString()));
    }

    public Mono<Long> countProductsByCategory(String category) {
        log.info("Counting products by category: {}", category);
        return searchRepositoryImpl.countByCategoryIgnoreCase(category)
                .doOnSuccess(count -> log.debug("Found {} products in category: {}", count, category))
                .doOnError(error -> log.error("Error counting products by category {}: {}", category, error.toString()));
    }

    public Flux<ProductDTO> getAllProducts() {
        log.info("Getting all products");
        return elasticsearchSearchRepository.findAll()
                .map(ProductDocument::toDTO)
                .doOnComplete(() -> log.debug("Completed getting all products"))
                .doOnError(error -> log.error("Error getting all products: {}", error.toString()));
    }
}
