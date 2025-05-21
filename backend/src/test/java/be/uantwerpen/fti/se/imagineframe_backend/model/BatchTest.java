package be.uantwerpen.fti.se.imagineframe_backend.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class BatchTest {

    @Test
    void testBatchDefaultConstructor() {
        Batch batch = new Batch();
        assertNotNull(batch);
        assertNotNull(batch.getAddedDate()); // Verificar que la fecha se inicializa correctamente
    }

    @Test
    void testBatchParameterizedConstructor() {
        Product product = new Product();

        int quantity = 10;
        double unitPrice = 20.5;

        Batch batch = new Batch(product, quantity, unitPrice);

        assertNotNull(batch);
        assertEquals(product, batch.getProduct());
        assertEquals(quantity, batch.getQuantity());
        assertEquals(unitPrice, batch.getUnitPrice(), 0.01);
        assertNotNull(batch.getAddedDate());
    }

    @Test
    void testGetProductId() {
        Product product = Mockito.mock(Product.class);
        when(product.getProductId()).thenReturn(2L);

        Batch batch = new Batch(product, 5, 15.0);

        assertEquals(2L, batch.getProductId());
    }
}
