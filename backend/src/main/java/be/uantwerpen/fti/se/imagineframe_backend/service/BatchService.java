package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.BatchAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.BatchRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class BatchService {
    final private BatchRepository batchRepository;
    final private ProductRepository productRepository;

    public BatchService(BatchRepository batchRepository, ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    public boolean priceGreaterThanZero(Double price) {
        return price > 0;
    }

    public boolean quantityGreaterThanZero(Integer quantity) {
        return quantity > 0;
    }

    public void saveBatch(BatchAddDto batch) {
        Product product = productRepository.findById(batch.getProductId()).orElse(null);
        Batch batchEntity = new Batch();
        batchEntity.setProduct(product);
        batchEntity.setQuantity(batch.getQuantity());
        batchEntity.setUnitPrice(batch.getUnitPrice());
        batchEntity.setExpirationDate(batch.getExpirationDate());
        batchRepository.save(batchEntity);
    }

    public void deleteBatchById(Long id) {
        batchRepository.deleteById(id);
    }


    public Optional<Batch> getBatchById(Long batchId) {
        return Optional.of(batchRepository.findById(batchId).get());
    }

    public Iterable<Batch> getAllBatches() {
        return batchRepository.findAll();
    }

    public Iterable<Batch> getBatchesExpiringSoonByIds(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(days);

        List<Batch> expiringBatches = new ArrayList<>();
        for (Batch batch : batchRepository.findAll()) {
            if (batch.getExpirationDate() != null
                    && batch.getExpirationDate().isAfter(now)
                    && batch.getExpirationDate().isBefore(limit)) {
                expiringBatches.add(batch);
            }
        }
        return expiringBatches;
    }
}
