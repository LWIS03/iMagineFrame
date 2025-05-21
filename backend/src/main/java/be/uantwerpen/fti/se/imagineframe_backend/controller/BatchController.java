package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.BatchExpiredException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.BatchAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.service.BatchService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/batch")
@Tag(name = "Batch Management", description = "API for managing product batches, including creation, retrieval, and deletion")
@SecurityRequirement(name = "Bearer Authentication")
public class BatchController {
    ProductService productService;
    BatchService batchService;

    public BatchController(ProductService productService, BatchService batchService) {
        this.productService = productService;
        this.batchService = batchService;
    }
    @Operation(summary = "Get all batches")
    @GetMapping
    public Iterable<Batch> getBatches() {
        return batchService.getAllBatches();
    }

    @Operation(summary = "Get batches expiring soon")
    @GetMapping("/expiringIn/{days}")
    public Iterable<Batch> getBatchesExpiringIn(@PathVariable int days) {
        return batchService.getBatchesExpiringSoonByIds(days);
    }

    @Operation(summary = "Get batch by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product_read')")
    public ResponseEntity<Batch> getBatchById(@PathVariable Long id) {
        Optional<Batch> batchOptional = batchService.getBatchById(id);
        if (batchOptional.isPresent()) {
            Batch batch = batchOptional.get();
            if (batch.getExpirationDate() != null && batch.getExpirationDate().isBefore(LocalDateTime.now())) {
                batchService.deleteBatchById(id);
                throw new BatchExpiredException(String.valueOf(id));
            }
            return new ResponseEntity<>(batch, HttpStatus.OK);
        }
        throw new EntityNotFoundException("Batch", String.valueOf(id));
    }

    @Operation(summary = "Add a new batch")
    @PostMapping
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> addBatch(@RequestBody BatchAddDto batch) {
        Long id = batch.getProductId();
        if (!productService.doesProductExist(id)) {
            throw new EntityNotFoundException("Product", String.valueOf(id));
        }
        double price = batch.getUnitPrice();
        if (!batchService.priceGreaterThanZero(price)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The price must be greater than 0");
        }
        int quantity = batch.getQuantity();
        if (!batchService.quantityGreaterThanZero(quantity)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The quantity must be greater than 0");
        }
        batchService.saveBatch(batch);
        return ResponseEntity.status(HttpStatus.OK).body("Bath successfully ordered");
    }

    @Operation(summary = "Delete a batch")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> deleteBatchById(@PathVariable Long id) {
        Optional<Batch> batch = batchService.getBatchById(id);
        if (batch.isPresent()) {
            batchService.deleteBatchById(id);
            return ResponseEntity.ok("Batch " + id + " deleted successfully.");
        } else {
            throw new EntityNotFoundException("Batch", String.valueOf(id));
        }
    }
}
