package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.BatchAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.BatchRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BatchServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BatchService batchService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPriceGreaterThan0() {
        assertTrue(batchService.priceGreaterThanZero(10.5));
        assertFalse(batchService.priceGreaterThanZero(0.0));
        assertFalse(batchService.priceGreaterThanZero(-5.0));
    }

    @Test
    void testQuantityGreaterThan0() {
        assertTrue(batchService.quantityGreaterThanZero(5));
        assertFalse(batchService.quantityGreaterThanZero(0));
        assertFalse(batchService.quantityGreaterThanZero(-3));
    }

    @Test
    void testSaveBatch_Success() {
        Long productId = 1L;
        Product product = new Product();

        BatchAddDto batchDto = new BatchAddDto(productId, 10, 15.5, null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        batchService.saveBatch(batchDto);

        verify(batchRepository, times(1)).save(any(Batch.class));
    }


    @Test
    void testDeleteBatchById() {
        Long batchId = 1L;

        batchService.deleteBatchById(batchId);

        verify(batchRepository, times(1)).deleteById(batchId);
    }

    @Test
    void testGetBatchById() {
        Long batchId = 1L;
        Batch batch = new Batch();
        when(batchRepository.findById(batchId)).thenReturn(Optional.of(batch));

        Optional<Batch> result = batchService.getBatchById(batchId);

        assertTrue(result.isPresent());
        verify(batchRepository, times(1)).findById(batchId);
    }

    @Test
    void testGetAllBatches() {
        Batch batch1 = new Batch();
        Batch batch2 = new Batch();
        List<Batch> batches = Arrays.asList(batch1, batch2);

        when(batchRepository.findAll()).thenReturn(batches);

        Iterable<Batch> result = batchService.getAllBatches();

        assertNotNull(result);
        assertEquals(2, ((List<Batch>) result).size());
        verify(batchRepository, times(1)).findAll();
    }

    @Test
    void testGetBatchesExpiringSoonByIds() {
        Batch batch1 = new Batch();
        Batch batch2 = new Batch();
        Batch batch3 = new Batch();

        batch1.setExpirationDate(LocalDateTime.now().plusDays(2));

        batch2.setExpirationDate(LocalDateTime.now().plusDays(1));

        batch3.setExpirationDate(LocalDateTime.now().plusDays(10));

        List<Batch> allBatches = Arrays.asList(batch1, batch2, batch3);
        when(batchRepository.findAll()).thenReturn(allBatches);

        Iterable<Batch> result = batchService.getBatchesExpiringSoonByIds(5);

        List<Batch> resultList = (List<Batch>) result;
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(batch1));
        assertTrue(resultList.contains(batch2));
        assertFalse(resultList.contains(batch3));
        verify(batchRepository, times(1)).findAll();
    }

}
