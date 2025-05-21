package be.uantwerpen.fti.se.imagineframe_backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.mockito.Mockito.*;

public class TagTest {

    private Tag tag;

    @BeforeEach
    void setUp() {
        tag = new Tag("Soda");
    }

    @Test
    void testTagCreation() {
        assert tag != null;
        assert tag.getName().equals("Soda");
        assert tag.getProducts().isEmpty();
    }

    @Test
    void testSetName() {
        tag.setName("refreshment");
        assert tag.getName().equals("refreshment");
    }

    @Test
    void testAddProduct() {
        Product product = mock(Product.class);
        when(product.getName()).thenReturn("CocaCola");

        tag.addProduct(product);

        assert tag.getProducts().contains(product);
        assert tag.getProducts().size() == 1;
        assert tag.getProductsName().contains("CocaCola");

        verify(product, times(1)).addTag(tag);
    }

    @Test
    void testRemoveProduct() {
        Product product = mock(Product.class);
        when(product.getName()).thenReturn("Pepsi");

        tag.addProduct(product);
        assert tag.getProducts().contains(product);
        tag.removeProduct(product);
        assert tag.getProducts().isEmpty();

        verify(product, times(1)).removeTag(tag);
    }

    @Test
    void testGetProductsName() {
        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        when(product1.getName()).thenReturn("Sprite");
        when(product2.getName()).thenReturn("Fanta");

        tag.addProduct(product1);
        tag.addProduct(product2);

        Set<String> productNames = tag.getProductsName();

        assert productNames.size() == 2;
        assert productNames.contains("Sprite");
        assert productNames.contains("Fanta");
    }
}
