package dev.makeev.recommendation.controller;

import dev.makeev.recommendation.dto.PopularProductRequest;
import dev.makeev.recommendation.dto.RatingRequest;
import dev.makeev.recommendation.model.PopularProduct;
import dev.makeev.recommendation.service.PopularProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class PopularProductController {

    private final PopularProductService popularProductService;

    @GetMapping("/popular")
    public Flux<PopularProduct> getTopPopularProducts() {
        log.info("GET /api/recommendations/popular - Getting top popular products");
        return popularProductService.getTopPopularProducts();
    }

    @GetMapping("/popular/category/{category}")
    public Flux<PopularProduct> getTopPopularProductsByCategory(@PathVariable String category) {
        log.info("GET /api/recommendations/popular/category/{} - Getting popular products by category", category);
        return popularProductService.getTopPopularProductsByCategory(category);
    }

    @GetMapping("/trending")
    public Flux<PopularProduct> getTrendingProducts() {
        log.info("GET /api/recommendations/trending - Getting trending products");
        return popularProductService.getTrendingProducts();
    }

    @GetMapping("/bestselling")
    public Flux<PopularProduct> getBestSellingProducts() {
        log.info("GET /api/recommendations/bestselling - Getting best selling products");
        return popularProductService.getBestSellingProducts();
    }

    @GetMapping("/highrated")
    public Flux<PopularProduct> getHighlyRatedProducts() {
        log.info("GET /api/recommendations/highrated - Getting highly rated products");
        return popularProductService.getHighlyRatedProducts();
    }

    @GetMapping("/popular/score/{minScore}")
    public Flux<PopularProduct> getPopularProductsByScore(@PathVariable Double minScore) {
        log.info("GET /api/recommendations/popular/score/{} - Getting products by score", minScore);
        return popularProductService.getPopularProductsByScore(minScore);
    }

    @PostMapping("/popular")
    public Mono<ResponseEntity<PopularProduct>> addOrUpdatePopularProduct(@Valid @RequestBody PopularProductRequest request) {
        log.info("POST /api/recommendations/popular - Adding/updating popular product: {}", request.productName());

        return popularProductService.addOrUpdatePopularProduct(
                        request.productId(),
                        request.productName(),
                        request.productDescription(),
                        request.category(),
                        request.price(),
                        request.imageUrl()
                ).map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/popular/{productId}/view")
    public Mono<ResponseEntity<PopularProduct>> incrementViewCount(@PathVariable String productId) {
        log.info("POST /api/recommendations/popular/{}/view - Incrementing view count", productId);

        return popularProductService.incrementViewCount(productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PostMapping("/popular/{productId}/purchase")
    public Mono<ResponseEntity<PopularProduct>> incrementPurchaseCount(@PathVariable String productId) {
        log.info("POST /api/recommendations/popular/{}/purchase - Incrementing purchase count", productId);

        return popularProductService.incrementPurchaseCount(productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @PutMapping("/popular/{productId}/rating")
    public Mono<ResponseEntity<PopularProduct>> updateRating(@PathVariable String productId,
                                                             @RequestBody RatingRequest request) {
        log.info("PUT /api/recommendations/popular/{}/rating - Updating rating to {}", productId, request.rating());

        return popularProductService.updateRating(productId, request.rating())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/popular/{productId}")
    public Mono<ResponseEntity<Void>> deletePopularProduct(@PathVariable String productId) {
        log.info("DELETE /api/recommendations/popular/{} - Deleting popular product", productId);

        return popularProductService.deletePopularProduct(productId)
                .map(v -> ResponseEntity.ok().<Void>build())
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/popular/all")
    public Flux<PopularProduct> getAllPopularProducts() {
        log.info("GET /api/recommendations/popular/all - Getting all popular products");
        return popularProductService.getAllPopularProducts();
    }

}
