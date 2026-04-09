package dev.makeev.inventory.controller;

import dev.makeev.inventory.dto.AddStockRequest;
import dev.makeev.inventory.dto.RemoveStockRequest;
import dev.makeev.inventory.dto.UpdateQuantityRequest;
import dev.makeev.inventory.model.Inventory;
import dev.makeev.inventory.service.InventoryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products/{productId}")
    public Mono<ResponseEntity<Inventory>> getInventoryByProductId(@PathVariable String productId) {
        log.info("REST: Get inventory request - productId: {}", productId);

        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    @GetMapping("/locations/{location}")
    public Flux<Inventory> getInventoryByLocation(@PathVariable String location) {
        log.info("REST: Get inventory by location - location: {}", location);

        return inventoryService.getInventoryByLocation(location);
    }

    @GetMapping("/low-stock")
    public Flux<Inventory> getLowStockItems(@RequestParam(defaultValue = "10") int threshold) {
        log.info("REST: Get low stock items - threshold: {}", threshold);

        return inventoryService.getLowStockItems(threshold);
    }

    @GetMapping("/out-of-stock")
    public Flux<Inventory> getOutOfStockItems() {
        log.info("REST: Get out of stock items");

        return inventoryService.getOutOfStockItems();
    }

    @PostMapping
    public Mono<ResponseEntity<Inventory>> createInventory(@Valid @RequestBody Inventory inventory) {
        log.info("REST: Create inventory request - productId: {}", inventory.getProductId());

        return inventoryService.createInventory(inventory)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/products/{productId}/quantity")
    public Mono<ResponseEntity<Inventory>> updateInventoryQuantity(
            @PathVariable String productId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        log.info("REST: Update inventory quantity - productId: {}, quantity: {}",
                productId, request.quantity());

        return inventoryService.updateInventoryQuantity(productId, request.quantity())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/products/{productId}/add-stock")
    public Mono<ResponseEntity<Inventory>> addStock(
            @PathVariable String productId,
            @Valid @RequestBody AddStockRequest request) {
        log.info("REST: Add stock - productId: {}, amount: {}", productId, request.amount());

        return inventoryService.addStock(productId, request.amount())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/products/{productId}/remove-stock")
    public Mono<ResponseEntity<Inventory>> removeStock(
            @PathVariable String productId,
            @Valid @RequestBody RemoveStockRequest request) {
        log.info("REST: Remove stock - productId: {}, amount: {}", productId, request.amount());

        return inventoryService.removeStock(productId, request.amount())
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/products/{productId}/in-stock")
    public Mono<ResponseEntity<Boolean>> isInStock(@PathVariable String productId) {
        log.info("REST: Check stock - productId: {}", productId);

        return inventoryService.isInStock(productId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping("/products/{productId}/stock-check")
    public Mono<ResponseEntity<Boolean>> hasEnoughStock(
            @PathVariable String productId,
            @RequestParam int requiredQuantity) {
        log.info("REST: Check stock availability - productId: {}, required: {}",
                productId, requiredQuantity);

        return inventoryService.hasEnoughStock(productId, requiredQuantity)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/products/{productId}")
    public Mono<ResponseEntity<Void>> deleteInventory(@PathVariable String productId) {
        log.info("REST: Delete inventory - productId: {}", productId);

        return inventoryService.deleteInventory(productId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().<Void>build());
    }
}