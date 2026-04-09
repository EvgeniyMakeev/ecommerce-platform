package dev.makeev.search.controller;

import dev.makeev.common.dto.ProductDTO;
import dev.makeev.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public Flux<ProductDTO> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (q != null && !q.trim().isEmpty()) {
            return searchService.searchProducts(q)
                    .skip((long) page * size)
                    .take(size);
        } else {
            return searchService.getAllProducts()
                    .skip((long) page * size)
                    .take(size);
        }
    }

    @GetMapping("/name/{name}")
    public Flux<ProductDTO> searchByName(
            @PathVariable String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.searchByName(name)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/category/{category}")
    public Flux<ProductDTO> searchByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.searchByCategory(category)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/tags")
    public Flux<ProductDTO> searchByTags(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.searchByTags(tag)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/all")
    public Flux<ProductDTO> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.getAllProducts()
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/price-range")
    public Flux<ProductDTO> searchByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.searchByPriceRange(minPrice, maxPrice)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/advanced")
    public Flux<ProductDTO> searchWithFilters(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String tag = tags != null && !tags.isEmpty() ? tags.getFirst() : null;
        
        return searchService.searchWithFilters(q, category, minPrice, maxPrice)
                .skip((long) page * size)
                .take(size);
    }

    @GetMapping("/suggestions")
    public Flux<ProductDTO> getProductSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        return searchService.getProductSuggestions(query)
                .take(limit);
    }

    @GetMapping("/recent")
    public Flux<ProductDTO> getRecentProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return searchService.getRecentProducts()
                .take(limit);
    }

    @GetMapping("/count/{category}")
    public Mono<ResponseEntity<Long>> countProductsByCategory(@PathVariable String category) {
        return searchService.countProductsByCategory(category)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/index")
    public Mono<ResponseEntity<ProductDTO>> indexProduct(@RequestBody ProductDTO productDTO) {
        return searchService.indexProduct(productDTO)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<ProductDTO>> updateProduct(@RequestBody ProductDTO productDTO) {
        return searchService.updateProduct(productDTO)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{productId}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String productId) {
        return searchService.deleteProduct(productId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
