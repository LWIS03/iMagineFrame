package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProductAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProductsExist_Exists() {
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        boolean exists = productService.doesProductExist(productId);

        assertTrue(exists);
        verify(productRepository, times(1)).existsById(productId);
    }

    @Test
    void testProductsExist_NotExists() {
        Long productId = 2L;
        when(productRepository.existsById(productId)).thenReturn(false);

        boolean exists = productService.doesProductExist(productId);

        assertFalse(exists);
        verify(productRepository, times(1)).existsById(productId);
    }

    @Test
    void testUpdateProductInformation_AllFieldsUpdated() {
        Product product = new Product();
        ProductAddDto productDto = new ProductAddDto();

        productDto.setName("New Name");
        productDto.setDescription("New Description");
        productDto.setImageUrl("http://example.com/image.jpg");
        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        productDto.setProperties(properties);

        Product updatedProduct = productService.updateProductInformation(product, productDto);

        assertEquals("New Name", updatedProduct.getName());
        assertEquals("New Description", updatedProduct.getDescription());
        assertEquals("http://example.com/image.jpg", updatedProduct.getImageUrl());
        assertEquals(properties, updatedProduct.getProperties());
    }

    @Test
    void testUpdateProductInformation_PartialUpdate() {
        Product product = new Product();
        product.setName("Old Name");
        product.setDescription("Old Description");
        product.setImageUrl("http://old-image.com/image.jpg");
        Map<String, String> oldProperties = new HashMap<>();
        oldProperties.put("key1", "oldValue");
        product.setProperties(oldProperties);

        ProductAddDto productDto = new ProductAddDto();
        productDto.setDescription("Updated Description");

        Product updatedProduct = productService.updateProductInformation(product, productDto);

        assertEquals("Old Name", updatedProduct.getName());
        assertEquals("Updated Description", updatedProduct.getDescription());
        assertEquals("http://old-image.com/image.jpg", updatedProduct.getImageUrl());
        assertEquals(oldProperties, updatedProduct.getProperties());
    }

    @Test
    void testUpdateProductInformation_NoUpdates() {
        Product product = new Product();
        product.setName("Existing Name");
        product.setDescription("Existing Description");
        product.setImageUrl("http://existing-image.com/image.jpg");
        Map<String, String> existingProperties = new HashMap<>();
        existingProperties.put("key1", "existingValue");
        product.setProperties(existingProperties);

        ProductAddDto productDto = new ProductAddDto();

        Product updatedProduct = productService.updateProductInformation(product, productDto);

        assertEquals("Existing Name", updatedProduct.getName());
        assertEquals("Existing Description", updatedProduct.getDescription());
        assertEquals("http://existing-image.com/image.jpg", updatedProduct.getImageUrl());
        assertEquals(existingProperties, updatedProduct.getProperties());
    }


    @Test
    void testRemoveImage_FileExists_FileDeleted() throws Exception {
        // Arrange
        String fileName = "test-image.jpg";
        File uploadsDir = new File("uploads");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdir();
        }
        File testFile = new File(uploadsDir, fileName);
        if (!testFile.exists()) {
            boolean created = testFile.createNewFile();
            assertTrue(created, "Failed to create dummy test file.");
        }
        assertTrue(testFile.exists(), "Test file should exist before deletion.");
        productService.removeImage(fileName);
        assertFalse(testFile.exists(), "Test file should be deleted.");
    }


}
