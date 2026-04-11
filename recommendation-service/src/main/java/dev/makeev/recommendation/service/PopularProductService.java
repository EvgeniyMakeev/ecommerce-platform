package dev.makeev.recommendation.service;

import dev.makeev.recommendation.model.PopularProduct;
import dev.makeev.recommendation.repository.PopularProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularProductService {
    
    private final PopularProductRepository popularProductRepository;

    public Flux<PopularProduct> getTopPopularProducts() {
        log.info("Getting top 10 popular products");
        return popularProductRepository.findTop10ByOrderByPopularityScoreDesc()
                .doOnNext(product -> log.debug("Found popular product: {} with score: {}", 
                        product.getProductName(), product.getPopularityScore()));
    }

    public Flux<PopularProduct> getTopPopularProductsByCategory(String category) {
        log.info("Getting top 5 popular products for category: {}", category);
        return popularProductRepository.findTop5ByCategoryOrderByPopularityScoreDesc(category)
                .doOnNext(product -> log.debug("Found popular product in category {}: {} with score: {}", 
                        category, product.getProductName(), product.getPopularityScore()));
    }

    public Flux<PopularProduct> getTrendingProducts() {
        log.info("Getting trending products");
        return popularProductRepository.findByViewCountGreaterThanOrderByViewCountDesc(10L)
                .filter(product -> product.getPurchaseCount() < product.getViewCount() * 0.1)
                .take(10)
                .doOnNext(product -> log.debug("Found trending product: {} with views: {}, purchases: {}", 
                        product.getProductName(), product.getViewCount(), product.getPurchaseCount()));
    }

    public Flux<PopularProduct> getBestSellingProducts() {
        log.info("Getting best selling products");
        return popularProductRepository.findByPurchaseCountGreaterThanOrderByPurchaseCountDesc(5L)
                .take(10)
                .doOnNext(product -> log.debug("Found best selling product: {} with purchases: {}", 
                        product.getProductName(), product.getPurchaseCount()));
    }

    public Flux<PopularProduct> getHighlyRatedProducts() {
        log.info("Getting highly rated products");
        return popularProductRepository.findByRatingGreaterThanOrderByRatingDesc(4.0)
                .take(10)
                .doOnNext(product -> log.debug("Found highly rated product: {} with rating: {}", 
                        product.getProductName(), product.getRating()));
    }

    public Flux<PopularProduct> getPopularProductsByScore(Double minScore) {
        log.info("Getting popular products with minimum score: {}", minScore);
        return popularProductRepository.findByPopularityScoreGreaterThanOrderByPopularityScoreDesc(minScore)
                .doOnNext(product -> log.debug("Found product with score {}: {}", 
                        product.getPopularityScore(), product.getProductName()));
    }

    public Mono<PopularProduct> addOrUpdatePopularProduct(String productId, String productName, 
                                                         String productDescription, String category, 
                                                         BigDecimal price, String imageUrl) {
        log.info("Adding or updating popular product: {}", productName);
        
        return popularProductRepository.findByProductId(productId)
                .switchIfEmpty(Mono.just(new PopularProduct(productId, productName, productDescription, 
                        category, price, imageUrl)))
                .flatMap(existingProduct -> {
                    existingProduct.setProductName(productName);
                    existingProduct.setProductDescription(productDescription);
                    existingProduct.setCategory(category);
                    existingProduct.setPrice(price);
                    existingProduct.setImageUrl(imageUrl);
                    existingProduct.setLastUpdated(LocalDateTime.now());
                    
                    return popularProductRepository.save(existingProduct);
                })
                .doOnSuccess(product -> log.info("Successfully saved popular product: {}", product.getProductName()));
    }

    public Mono<PopularProduct> incrementViewCount(String productId) {
        log.info("Incrementing view count for product: {}", productId);
        
        return popularProductRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)))
                .flatMap(product -> {
                    product.incrementViewCount();
                    return popularProductRepository.save(product);
                })
                .doOnSuccess(product -> log.debug("Incremented view count for product: {} to {}", 
                        product.getProductName(), product.getViewCount()));
    }

    public Mono<PopularProduct> incrementPurchaseCount(String productId) {
        log.info("Incrementing purchase count for product: {}", productId);
        
        return popularProductRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)))
                .flatMap(product -> {
                    product.incrementPurchaseCount();
                    return popularProductRepository.save(product);
                })
                .doOnSuccess(product -> log.debug("Incremented purchase count for product: {} to {}", 
                        product.getProductName(), product.getPurchaseCount()));
    }

    public Mono<PopularProduct> updateRating(String productId, Double rating) {
        log.info("Updating rating for product: {} to {}", productId, rating);
        
        return popularProductRepository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found: " + productId)))
                .flatMap(product -> {
                    product.updateRating(rating);
                    return popularProductRepository.save(product);
                })
                .doOnSuccess(product -> log.debug("Updated rating for product: {} to {}", 
                        product.getProductName(), product.getRating()));
    }

    public Mono<Void> deletePopularProduct(String productId) {
        log.info("Deleting popular product: {}", productId);
        
        return popularProductRepository.deleteByProductId(productId)
                .doOnSuccess(unused -> log.info("Successfully deleted popular product: {}", productId));
    }

    public Flux<PopularProduct> getAllPopularProducts() {
        log.info("Getting all popular products sorted by popularity score");
        return popularProductRepository.findAllByOrderByPopularityScoreDesc()
                .doOnNext(product -> log.debug("Found product: {} with score: {}", 
                        product.getProductName(), product.getPopularityScore()));
    }
}
