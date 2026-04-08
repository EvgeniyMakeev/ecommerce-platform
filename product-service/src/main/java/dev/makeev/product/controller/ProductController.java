package dev.makeev.product.controller;

import dev.makeev.common.dto.ProductDTO;
import dev.makeev.product.service.ProductServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductServiceInterface productService;

    @PostMapping
    public Mono<ResponseEntity<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST: Create product request - {}", productDTO.name());
        return productService.createProduct(productDTO)
                .map(product -> ResponseEntity.status(HttpStatus.CREATED).body(product));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductDTO>> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("REST: Update product request - {}", id);
        return productService.updateProduct(id, productDTO)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
        log.info("REST: Delete product request - {}", id);
        return productService.deleteProduct(id)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductDTO>> getProduct(@PathVariable String id) {
        log.debug("REST: Get product request - {}", id);
        return productService.getProduct(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public ResponseEntity<Flux<ProductDTO>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {

        Flux<ProductDTO> products;

        if (search != null && !search.isEmpty()) {
            log.debug("REST: Search products - {}", search);
            products = productService.searchProducts(search);
        } else if (category != null && !category.isEmpty()) {
            log.debug("REST: Get products by category - {}", category);
            products = productService.getProductsByCategory(category);
        } else {
            log.debug("REST: Get all products");
            products = productService.getAllProducts();
        }

        return ResponseEntity.ok(products);
    }
}
