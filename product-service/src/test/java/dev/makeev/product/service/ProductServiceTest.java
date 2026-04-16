package dev.makeev.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.makeev.common.config.ProductBindings;
import dev.makeev.common.dto.ProductDTO;
import dev.makeev.common.events.ProductEvent;
import dev.makeev.product.model.Product;
import dev.makeev.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.dao.DataAccessResourceFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StreamBridge streamBridge;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private final String TEST_ID = UUID.randomUUID().toString();
    private final String TEST_NAME = "Test Product";
    private final String TEST_DESCRIPTION = "Test Description";
    private final BigDecimal TEST_PRICE = new BigDecimal("99.99");
    private final String TEST_CATEGORY = "Electronics";
    private final List<String> TEST_TAGS = Arrays.asList("tag1", "tag2");
    private final String TEST_IMAGE_URL = "https://example.com/image.jpg";

    @BeforeEach
    void setUp() throws JsonProcessingException {
        testProduct = Product.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .description(TEST_DESCRIPTION)
                .price(TEST_PRICE)
                .category(TEST_CATEGORY)
                .tags("[\"tag1\",\"tag2\"]")
                .imageUrl(TEST_IMAGE_URL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        lenient().when(objectMapper.writeValueAsString(any())).thenReturn("[]");
        lenient().when(objectMapper.readValue(anyString(), eq(String[].class))).thenReturn(new String[]{"tag1", "tag2"});
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProduct_Success() {
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

        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));
        when(streamBridge.send(anyString(), any(ProductEvent.class))).thenReturn(true);

        StepVerifier.create(productService.createProduct(createDTO))
                .assertNext(dto -> {
                    assertEquals(TEST_ID, dto.id());
                    assertEquals(TEST_NAME, dto.name());
                    assertEquals(TEST_DESCRIPTION, dto.description());
                    assertEquals(TEST_PRICE, dto.price());
                    assertEquals(TEST_CATEGORY, dto.category());
                    assertEquals(TEST_TAGS, dto.tags());
                    assertEquals(TEST_IMAGE_URL, dto.imageUrl());
                    assertNotNull(dto.createdAt());
                    assertNotNull(dto.updatedAt());
                })
                .verifyComplete();

        verify(productRepository).save(any(Product.class));
        verify(streamBridge).send(eq(ProductBindings.PRODUCT_EVENTS_OUTPUT.getBindingName()), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should handle create product error")
    void createProduct_Error() {
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

        when(productRepository.save(any(Product.class)))
                .thenReturn(Mono.error(new DataAccessResourceFailureException("Database error")));

        StepVerifier.create(productService.createProduct(createDTO))
                .expectError(DataAccessResourceFailureException.class)
                .verify();

        verify(productRepository).save(any(Product.class));
        verify(streamBridge, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProduct_Success() {
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

        when(productRepository.findById(TEST_ID)).thenReturn(Mono.just(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(testProduct));
        when(streamBridge.send(anyString(), any(ProductEvent.class))).thenReturn(true);

        StepVerifier.create(productService.updateProduct(TEST_ID, updateDTO))
                .assertNext(dto -> {
                    assertEquals(TEST_ID, dto.id());
                    assertNotNull(dto.name());
                    assertNotNull(dto.description());
                    assertNotNull(dto.price());
                    assertNotNull(dto.category());
                })
                .verifyComplete();

        verify(productRepository).findById(TEST_ID);
        verify(productRepository).save(any(Product.class));
        verify(streamBridge).send(eq(ProductBindings.PRODUCT_EVENTS_OUTPUT.getBindingName()), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should return error when updating non-existent product")
    void updateProduct_NotFound() {
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

        when(productRepository.findById(TEST_ID)).thenReturn(Mono.empty());

        StepVerifier.create(productService.updateProduct(TEST_ID, updateDTO))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Product not found: " + TEST_ID))
                .verify();

        verify(productRepository).findById(TEST_ID);
        verify(productRepository, never()).save(any());
        verify(streamBridge, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void deleteProduct_Success() {
        when(productRepository.findById(TEST_ID)).thenReturn(Mono.just(testProduct));
        when(productRepository.deleteById(TEST_ID)).thenReturn(Mono.empty());
        when(streamBridge.send(anyString(), any(ProductEvent.class))).thenReturn(true);

        StepVerifier.create(productService.deleteProduct(TEST_ID))
                .verifyComplete();

        verify(productRepository).findById(TEST_ID);
        verify(productRepository).deleteById(TEST_ID);
        verify(streamBridge).send(eq("productEvents-out-0"), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should return error when deleting non-existent product")
    void deleteProduct_NotFound() {
        when(productRepository.findById(TEST_ID)).thenReturn(Mono.empty());

        StepVerifier.create(productService.deleteProduct(TEST_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Product not found: " + TEST_ID))
                .verify();

        verify(productRepository).findById(TEST_ID);
        verify(productRepository, never()).deleteById(anyString());
        verify(streamBridge, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProduct_Success() {
        when(productRepository.findById(TEST_ID)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(productService.getProduct(TEST_ID))
                .assertNext(dto -> {
                    assertEquals(TEST_ID, dto.id());
                    assertEquals(TEST_NAME, dto.name());
                    assertEquals(TEST_DESCRIPTION, dto.description());
                    assertEquals(TEST_PRICE, dto.price());
                    assertEquals(TEST_CATEGORY, dto.category());
                    assertEquals(TEST_TAGS, dto.tags());
                    assertEquals(TEST_IMAGE_URL, dto.imageUrl());
                })
                .verifyComplete();

        verify(productRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("Should return error when getting non-existent product")
    void getProduct_NotFound() {
        when(productRepository.findById(TEST_ID)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProduct(TEST_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException &&
                        ex.getMessage().equals("Product not found: " + TEST_ID))
                .verify();

        verify(productRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("Should get all products successfully")
    void getAllProducts_Success() {
        Product product2 = Product.builder()
                .id(UUID.randomUUID().toString())
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("49.99"))
                .category("Books")
                .tags("[]")
                .imageUrl("https://example.com/image2.jpg")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productRepository.findAll()).thenReturn(Flux.just(testProduct, product2));

        StepVerifier.create(productService.getAllProducts())
                .expectNextCount(2)
                .verifyComplete();

        verify(productRepository).findAll();
    }

    @Test
    @DisplayName("Should get products by category successfully")
    void getProductsByCategory_Success() {
        when(productRepository.findByCategory(TEST_CATEGORY)).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.getProductsByCategory(TEST_CATEGORY))
                .assertNext(dto -> {
                    assertEquals(TEST_ID, dto.id());
                    assertEquals(TEST_CATEGORY, dto.category());
                })
                .verifyComplete();

        verify(productRepository).findByCategory(TEST_CATEGORY);
    }

    @Test
    @DisplayName("Should search products successfully")
    void searchProducts_Success() {
        String searchTerm = "Test";
        when(productRepository.searchByName(searchTerm)).thenReturn(Flux.just(testProduct));

        StepVerifier.create(productService.searchProducts(searchTerm))
                .assertNext(dto -> {
                    assertEquals(TEST_ID, dto.id());
                    assertTrue(dto.name().contains(searchTerm));
                })
                .verifyComplete();

        verify(productRepository).searchByName(searchTerm);
    }

    @Test
    @DisplayName("Should handle JSON processing error in tags serialization")
    void createProduct_TagsSerializationError() throws JsonProcessingException {
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

        when(objectMapper.writeValueAsString(any(List.class)))
                .thenThrow(new JsonProcessingException("JSON error") {});

        StepVerifier.create(productService.createProduct(createDTO))
                .expectError(JsonProcessingException.class)
                .verify();

        verify(productRepository, never()).save(any());
        verify(streamBridge, never()).send(anyString(), any());
    }

    @Test
    @DisplayName("Should handle JSON processing error in tags deserialization")
    void getProduct_TagsDeserializationError() throws JsonProcessingException {
        Product productWithInvalidTags = Product.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .description(TEST_DESCRIPTION)
                .price(TEST_PRICE)
                .category(TEST_CATEGORY)
                .tags("invalid-json")
                .imageUrl(TEST_IMAGE_URL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(productRepository.findById(TEST_ID)).thenReturn(Mono.just(productWithInvalidTags));
        when(objectMapper.readValue(anyString(), eq(String[].class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        StepVerifier.create(productService.getProduct(TEST_ID))
                .expectError(RuntimeException.class)
                .verify();

        verify(productRepository).findById(TEST_ID);
    }

    @Test
    @DisplayName("Should handle empty tags list correctly")
    void createProduct_EmptyTags() throws JsonProcessingException {
        ProductDTO createDTO = new ProductDTO(
                null,
                TEST_NAME,
                TEST_DESCRIPTION,
                TEST_PRICE,
                TEST_CATEGORY,
                List.of(),
                TEST_IMAGE_URL,
                null,
                null
        );

        Product productWithEmptyTags = Product.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .description(TEST_DESCRIPTION)
                .price(TEST_PRICE)
                .category(TEST_CATEGORY)
                .tags("[]")
                .imageUrl(TEST_IMAGE_URL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(objectMapper.writeValueAsString(List.of())).thenReturn("[]");
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(productWithEmptyTags));
        when(streamBridge.send(anyString(), any(ProductEvent.class))).thenReturn(true);
        when(objectMapper.readValue("[]", String[].class)).thenReturn(new String[0]);

        StepVerifier.create(productService.createProduct(createDTO))
                .assertNext(dto -> assertTrue(dto.tags().isEmpty()))
                .verifyComplete();

        verify(productRepository).save(any(Product.class));
        verify(streamBridge).send(eq(ProductBindings.PRODUCT_EVENTS_OUTPUT.getBindingName()), any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should handle null tags correctly")
    void createProduct_NullTags() throws JsonProcessingException {
        ProductDTO createDTO = new ProductDTO(
                null,
                TEST_NAME,
                TEST_DESCRIPTION,
                TEST_PRICE,
                TEST_CATEGORY,
                null,
                TEST_IMAGE_URL,
                null,
                null
        );

        Product productWithNullTags = Product.builder()
                .id(TEST_ID)
                .name(TEST_NAME)
                .description(TEST_DESCRIPTION)
                .price(TEST_PRICE)
                .category(TEST_CATEGORY)
                .tags("[]")
                .imageUrl(TEST_IMAGE_URL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(objectMapper.writeValueAsString(null)).thenReturn("[]");
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(productWithNullTags));
        when(streamBridge.send(anyString(), any(ProductEvent.class))).thenReturn(true);
        when(objectMapper.readValue("[]", String[].class)).thenReturn(new String[0]);

        StepVerifier.create(productService.createProduct(createDTO))
                .assertNext(dto -> assertTrue(dto.tags().isEmpty()))
                .verifyComplete();

        verify(productRepository).save(any(Product.class));
        verify(streamBridge).send(eq(ProductBindings.PRODUCT_EVENTS_OUTPUT.getBindingName()), any(ProductEvent.class));
    }
}
