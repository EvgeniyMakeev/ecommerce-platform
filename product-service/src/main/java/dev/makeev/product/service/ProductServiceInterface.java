package dev.makeev.product.service;

import dev.makeev.common.dto.ProductDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for product operations
 */
public interface ProductServiceInterface {
    
    Mono<ProductDTO> createProduct(ProductDTO productDTO);
    
    Mono<ProductDTO> updateProduct(String id, ProductDTO productDTO);
    
    Mono<Void> deleteProduct(String id);
    
    Mono<ProductDTO> getProduct(String id);
    
    Flux<ProductDTO> getAllProducts();
    
    Flux<ProductDTO> getProductsByCategory(String category);
    
    Flux<ProductDTO> searchProducts(String searchTerm);
}
