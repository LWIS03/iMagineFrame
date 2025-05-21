package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.BatchAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.service.BatchService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private BatchService batchService;

    @InjectMocks
    private BatchController batchController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Batch batch;
    private BatchAddDto batchAddDto;

    @Autowired
    public BatchControllerTest(ProductService productService, BatchService batchService) {
        this.productService = productService;
        this.batchService = batchService;
    }

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        batch = new Batch();
        batch.setId(1L);
        batch.setExpirationDate(LocalDateTime.now().plusDays(10));

        batchAddDto = new BatchAddDto();
        batchAddDto.setProductId(1L);
        batchAddDto.setUnitPrice(10.0);
        batchAddDto.setQuantity(5);

        mockMvc = MockMvcBuilders.standaloneSetup(batchController).setControllerAdvice(GlobalExceptionHandler.class).build();
    }



    @Test
    public void testGetBatchByIdNotFound() throws Exception {
        when(batchService.getBatchById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/batch/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Batch not found: 1"));
    }

    @Test
    public void testGetBatchByIdExpired() throws Exception {
        Batch expiredBatch = new Batch();
        expiredBatch.setId(2L);
        expiredBatch.setExpirationDate(LocalDateTime.now().minusDays(1));
        when(batchService.getBatchById(2L)).thenReturn(Optional.of(expiredBatch));

        mockMvc.perform(get("/batch/2"))
                .andExpect(status().isGone())
                .andExpect(content().string("Batch with id 2 is expired"));

        verify(batchService, times(1)).deleteBatchById(2L);
    }

    @Test
    public void testAddBatchSuccessfully() throws Exception {
        when(productService.doesProductExist(1L)).thenReturn(true);
        when(batchService.priceGreaterThanZero(10.0)).thenReturn(true);
        when(batchService.quantityGreaterThanZero(5)).thenReturn(true);

        mockMvc.perform(post("/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchAddDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Bath successfully ordered"));
    }

    @Test
    public void testAddBatchProductNotFound() throws Exception {
        when(productService.doesProductExist(1L)).thenReturn(false);

        mockMvc.perform(post("/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchAddDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Product not found: 1"));
    }

    @Test
    public void testAddBatchInvalidPrice() throws Exception {
        when(productService.doesProductExist(1L)).thenReturn(true);
        when(batchService.priceGreaterThanZero(10.0)).thenReturn(false);

        mockMvc.perform(post("/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchAddDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The price must be greater than 0"));
    }

    @Test
    public void testAddBatchInvalidQuantity() throws Exception {
        when(productService.doesProductExist(1L)).thenReturn(true);
        when(batchService.priceGreaterThanZero(10.0)).thenReturn(true);
        when(batchService.quantityGreaterThanZero(5)).thenReturn(false);

        mockMvc.perform(post("/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchAddDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("The quantity must be greater than 0"));
    }

    @Test
    public void testDeleteBatchByIdFound() throws Exception {
        when(batchService.getBatchById(1L)).thenReturn(Optional.of(batch));

        mockMvc.perform(delete("/batch/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch 1 deleted successfully."));

        verify(batchService, times(1)).deleteBatchById(1L);
    }

    @Test
    public void testDeleteBatchByIdNotFound() throws Exception {
        when(batchService.getBatchById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/batch/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Batch not found: 1"));
    }
}
