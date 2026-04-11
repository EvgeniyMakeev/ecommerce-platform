package dev.makeev.recommendation;

import dev.makeev.recommendation.model.PopularProduct;
import dev.makeev.recommendation.repository.PopularProductRepository;
import dev.makeev.recommendation.service.PopularProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularProductService Tests")
class PopularProductServiceTest {

    @Mock
    private PopularProductRepository popularProductRepository;

    @InjectMocks
    private PopularProductService popularProductService;

    @Test
    @DisplayName("Should get top popular products")
    void testGetTopPopularProducts() {
        PopularProduct product1 = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product1.setPopularityScore(4.5);
        
        PopularProduct product2 = new PopularProduct("2", "Samsung Galaxy", "Latest Samsung", "Electronics", 
                new BigDecimal("999.99"), "image2.jpg");
        product2.setPopularityScore(3.8);
        
        when(popularProductRepository.findTop10ByOrderByPopularityScoreDesc())
                .thenReturn(Flux.just(product1, product2));

        StepVerifier.create(popularProductService.getTopPopularProducts())
                .expectNext(product1)
                .expectNext(product2)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get top popular products by category")
    void testGetTopPopularProductsByCategory() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setPopularityScore(4.5);
        
        when(popularProductRepository.findTop5ByCategoryOrderByPopularityScoreDesc("Electronics"))
                .thenReturn(Flux.just(product));

        StepVerifier.create(popularProductService.getTopPopularProductsByCategory("Electronics"))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get trending products")
    void testGetTrendingProducts() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setViewCount(100L);
        product.setPurchaseCount(5L);
        
        when(popularProductRepository.findByViewCountGreaterThanOrderByViewCountDesc(10L))
                .thenReturn(Flux.just(product));

        StepVerifier.create(popularProductService.getTrendingProducts())
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get best selling products")
    void testGetBestSellingProducts() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setPurchaseCount(10L);
        
        when(popularProductRepository.findByPurchaseCountGreaterThanOrderByPurchaseCountDesc(5L))
                .thenReturn(Flux.just(product));

        StepVerifier.create(popularProductService.getBestSellingProducts())
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get highly rated products")
    void testGetHighlyRatedProducts() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setRating(4.5);
        
        when(popularProductRepository.findByRatingGreaterThanOrderByRatingDesc(4.0))
                .thenReturn(Flux.just(product));

        StepVerifier.create(popularProductService.getHighlyRatedProducts())
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should add or update popular product")
    void testAddOrUpdatePopularProduct() {
        PopularProduct existingProduct = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        
        when(popularProductRepository.findByProductId("1"))
                .thenReturn(Mono.just(existingProduct));
        when(popularProductRepository.save(any(PopularProduct.class)))
                .thenReturn(Mono.just(existingProduct));

        StepVerifier.create(popularProductService.addOrUpdatePopularProduct("1", "iPhone 15 Pro", "Updated iPhone",
                "Electronics", new BigDecimal("1299.99"), "image1.jpg"))
                .expectNext(existingProduct)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should increment view count")
    void testIncrementViewCount() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setViewCount(10L);
        
        when(popularProductRepository.findByProductId("1"))
                .thenReturn(Mono.just(product));
        when(popularProductRepository.save(any(PopularProduct.class)))
                .thenReturn(Mono.just(product));

        StepVerifier.create(popularProductService.incrementViewCount("1"))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should increment purchase count")
    void testIncrementPurchaseCount() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        product.setPurchaseCount(5L);
        
        when(popularProductRepository.findByProductId("1"))
                .thenReturn(Mono.just(product));
        when(popularProductRepository.save(any(PopularProduct.class)))
                .thenReturn(Mono.just(product));

        StepVerifier.create(popularProductService.incrementPurchaseCount("1"))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should update rating")
    void testUpdateRating() {
        PopularProduct product = new PopularProduct("1", "iPhone 15", "Latest iPhone", "Electronics",
                new BigDecimal("1199.99"), "image1.jpg");
        
        when(popularProductRepository.findByProductId("1"))
                .thenReturn(Mono.just(product));
        when(popularProductRepository.save(any(PopularProduct.class)))
                .thenReturn(Mono.just(product));

        StepVerifier.create(popularProductService.updateRating("1", 4.5))
                .expectNext(product)
                .verifyComplete();
    }
}
