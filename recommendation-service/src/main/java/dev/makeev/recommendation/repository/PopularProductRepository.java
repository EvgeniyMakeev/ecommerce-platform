package dev.makeev.recommendation.repository;

import dev.makeev.recommendation.model.PopularProduct;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface PopularProductRepository extends R2dbcRepository<PopularProduct, Long> {
    
    Mono<PopularProduct> findByProductId(String productId);
    
    Flux<PopularProduct> findByCategory(String category);
    
    Flux<PopularProduct> findTop10ByOrderByPopularityScoreDesc();
    
    Flux<PopularProduct> findTop5ByCategoryOrderByPopularityScoreDesc(String category);
    
    Flux<PopularProduct> findByPopularityScoreGreaterThanOrderByPopularityScoreDesc(Double minScore);
    
    Flux<PopularProduct> findByPurchaseCountGreaterThanOrderByPurchaseCountDesc(Long minPurchases);
    
    Flux<PopularProduct> findByViewCountGreaterThanOrderByViewCountDesc(Long minViews);
    
    Flux<PopularProduct> findByRatingGreaterThanOrderByRatingDesc(Double minRating);
    
    Flux<PopularProduct> findAllByOrderByPopularityScoreDesc();
    
    Flux<PopularProduct> findByProductIdIn(List<String> productIds);
    
    Mono<Void> deleteByProductId(String productId);
}
