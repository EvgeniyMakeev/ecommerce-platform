package dev.makeev.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.makeev.common.dto.ProductDTO;
import dev.makeev.product.exception.GlobalExceptionHandler;
import dev.makeev.product.service.ProductServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Controller Tests")
class ProductControllerTest {

    @Mock
    private ProductServiceInterface productService;

    @InjectMocks
    private ProductController productController;

    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;
    private ProductDTO testProductDTO;
    private final String TEST_ID = UUID.randomUUID().toString();
    private final String TEST_NAME = "Test Product";
    private final String TEST_DESCRIPTION = "Test Description";
    private final BigDecimal TEST_PRICE = new BigDecimal("99.99");
    private final String TEST_CATEGORY = "Electronics";
    private final List<String> TEST_TAGS = Arrays.asList("tag1", "tag2");
    private final String TEST_IMAGE_URL = "https://example.com/image.jpg";

    @BeforeEach
    void setUp() {
        productController = new ProductController(productService);
        webTestClient = WebTestClient.bindToController(productController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        testProductDTO = new ProductDTO(
                TEST_ID,
                TEST_NAME,
                TEST_DESCRIPTION,
                TEST_PRICE,
                TEST_CATEGORY,
                TEST_TAGS,
                TEST_IMAGE_URL,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_Success() throws Exception {
        ProductDTO createDTO = new ProductDTO(
                null,
                TEST_NAME,
                TEST_DESCRIPTION,
                TEST_PRICE,
                TEST_CATEGORY,
                TEST_TAGS,
                TEST_IMAGE_URL,
                null,
                null
        );

        when(productService.createProduct(any(ProductDTO.class))).thenReturn(Mono.just(testProductDTO));

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(createDTO))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(TEST_ID)
                .jsonPath("$.name").isEqualTo(TEST_NAME)
                .jsonPath("$.description").isEqualTo(TEST_DESCRIPTION)
                .jsonPath("$.price").isEqualTo(TEST_PRICE.doubleValue())
                .jsonPath("$.category").isEqualTo(TEST_CATEGORY)
                .jsonPath("$.tags").isArray()
                .jsonPath("$.imageUrl").isEqualTo(TEST_IMAGE_URL);
    }

    @Test
    @DisplayName("Should return 400 when creating product with invalid data")
    void createProduct_InvalidData() throws Exception {
        ProductDTO invalidDTO = new ProductDTO(
                null,
                "",
                TEST_DESCRIPTION,
                TEST_PRICE,
                TEST_CATEGORY,
                TEST_TAGS,
                TEST_IMAGE_URL,
                null,
                null
        );

        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(invalidDTO))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_Success() throws Exception {
        ProductDTO updateDTO = new ProductDTO(
                null,
                "Updated Name",
                "Updated Description",
                new BigDecimal("199.99"),
                "Updated Category",
                Arrays.asList("new-tag"),
                "https://example.com/new-image.jpg",
                null,
                null
        );

        ProductDTO updatedDTO = new ProductDTO(
                TEST_ID,
                "Updated Name",
                "Updated Description",
                new BigDecimal("199.99"),
                "Updated Category",
                Arrays.asList("new-tag"),
                "https://example.com/new-image.jpg",
                Instant.now(),
                Instant.now()
        );

        when(productService.updateProduct(eq(TEST_ID), any(ProductDTO.class))).thenReturn(Mono.just(updatedDTO));

        webTestClient.put()
                .uri("/api/products/{id}", TEST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDTO))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(TEST_ID)
                .jsonPath("$.name").isEqualTo("Updated Name")
                .jsonPath("$.description").isEqualTo("Updated Description")
                .jsonPath("$.price").isEqualTo(199.99)
                .jsonPath("$.category").isEqualTo("Updated Category");
    }

    @Test
    @DisplayName("Should return 400 when updating non-existent product")
    void updateProduct_NotFound() throws Exception {
        ProductDTO updateDTO = new ProductDTO(
                null,
                "Updated Name",
                "Updated Description",
                new BigDecimal("199.99"),
                "Updated Category",
                Arrays.asList("new-tag"),
                "https://example.com/new-image.jpg",
                null,
                null
        );

        when(productService.updateProduct(eq(TEST_ID), any(ProductDTO.class)))
                .thenReturn(Mono.error(new RuntimeException("Product not found: " + TEST_ID)));

        webTestClient.put()
                .uri("/api/products/{id}", TEST_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(updateDTO))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Product not found: " + TEST_ID);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_Success() {
        when(productService.deleteProduct(TEST_ID)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/products/{id}", TEST_ID)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("Should return 400 when deleting non-existent product")
    void deleteProduct_NotFound() {
        when(productService.deleteProduct(TEST_ID))
                .thenReturn(Mono.error(new RuntimeException("Product not found: " + TEST_ID)));

        webTestClient.delete()
                .uri("/api/products/{id}", TEST_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Product not found: " + TEST_ID);
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProduct_Success() {
        when(productService.getProduct(TEST_ID)).thenReturn(Mono.just(testProductDTO));

        webTestClient.get()
                .uri("/api/products/{id}", TEST_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(TEST_ID)
                .jsonPath("$.name").isEqualTo(TEST_NAME)
                .jsonPath("$.description").isEqualTo(TEST_DESCRIPTION)
                .jsonPath("$.price").isEqualTo(TEST_PRICE.doubleValue())
                .jsonPath("$.category").isEqualTo(TEST_CATEGORY)
                .jsonPath("$.tags").isArray()
                .jsonPath("$.imageUrl").isEqualTo(TEST_IMAGE_URL);
    }

    @Test
    @DisplayName("Should return 400 when getting non-existent product")
    void getProduct_NotFound() {
        when(productService.getProduct(TEST_ID))
                .thenReturn(Mono.error(new RuntimeException("Product not found: " + TEST_ID)));

        webTestClient.get()
                .uri("/api/products/{id}", TEST_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Product not found: " + TEST_ID);
    }

    @Test
    @DisplayName("Should get all products successfully")
    void getAllProducts_Success() {
        ProductDTO product2 = new ProductDTO(
                UUID.randomUUID().toString(),
                "Product 2",
                "Description 2",
                new BigDecimal("49.99"),
                "Books",
                Arrays.asList("book"),
                "https://example.com/image2.jpg",
                Instant.now(),
                Instant.now()
        );

        when(productService.getAllProducts()).thenReturn(Flux.just(testProductDTO, product2));

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(TEST_ID)
                .jsonPath("$[1].name").isEqualTo("Product 2");
    }

    @Test
    @DisplayName("Should get products by category successfully")
    void getProductsByCategory_Success() {
        when(productService.getProductsByCategory(TEST_CATEGORY)).thenReturn(Flux.just(testProductDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("category", TEST_CATEGORY)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].category").isEqualTo(TEST_CATEGORY);
    }

    @Test
    @DisplayName("Should search products successfully")
    void searchProducts_Success() {
        String searchTerm = "Test";
        when(productService.searchProducts(searchTerm)).thenReturn(Flux.just(testProductDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("search", searchTerm)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo(TEST_NAME);
    }

    @Test
    @DisplayName("Should prioritize search over category when both parameters provided")
    void getProducts_SearchAndCategory_SearchTakesPriority() {
        String searchTerm = "Test";
        String category = "Electronics";
        when(productService.searchProducts(searchTerm)).thenReturn(Flux.just(testProductDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("category", category)
                        .queryParam("search", searchTerm)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1);

        verify(productService).searchProducts(searchTerm);
    }

    @Test
    @DisplayName("Should return empty array when no products found")
    void getAllProducts_Empty() {
        when(productService.getAllProducts()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle empty category parameter")
    void getProducts_EmptyCategory() {
        when(productService.getAllProducts()).thenReturn(Flux.just(testProductDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("category", "")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1);

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("Should handle empty search parameter")
    void getProducts_EmptySearch() {
        when(productService.getAllProducts()).thenReturn(Flux.just(testProductDTO));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products")
                        .queryParam("search", "")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(1);

        verify(productService).getAllProducts();
    }

    @Test
    @DisplayName("Should handle malformed JSON in request body")
    void createProduct_MalformedJson() {
        webTestClient.post()
                .uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ invalid json }")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void handleServiceException() {
        when(productService.getProduct(TEST_ID))
                .thenReturn(Mono.error(new RuntimeException("Database connection failed")));

        webTestClient.get()
                .uri("/api/products/{id}", TEST_ID)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.message").isEqualTo("Database connection failed")
                .jsonPath("$.timestamp").exists();
    }
}
