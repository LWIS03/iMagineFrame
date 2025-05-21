package be.uantwerpen.fti.se.imagineframe_backend.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ProductTest {

    @Test
    void TestDefaultConstructor() {
        Product product = new Product();

        assert product.getName() == "default_name";
        assert product.getDescription() == "default_description";
        assert product.getProperties().isEmpty();
    }

    @Test
    void TestConstructorWithoutProperties() {
        Product product = new Product("Laptop", "Gaming Laptop", new HashMap<>(), "");

        assert product.getName().equals("Laptop");
        assert product.getDescription().equals("Gaming Laptop");
        assert product.getProperties().isEmpty();
    }

    @Test
    void TestConstructorWithProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("RAM", "16GB");
        properties.put("Storage", "1TB SSD");

        Product product = new Product("Laptop", "Gaming Laptop", properties, "");

        assert product.getName().equals("Laptop");
        assert product.getDescription().equals("Gaming Laptop");

        assert product.getProperties().size() == properties.size();
        assert product.getProperties().get("RAM").equals("16GB");
        assert product.getProperties().get("Storage").equals("1TB SSD");
    }

    @Test
    void TestSettersAndGetters() {
        Product product = new Product();

        product.setName("Smartphone");
        product.setDescription("Android phone");

        Map<String, String> properties = new HashMap<>();
        properties.put("Display", "OLED");
        properties.put("Connectivity", "5G Support");
        product.setProperties(properties);


        assert product.getName().equals("Smartphone");
        assert product.getDescription().equals("Android phone");

        assert product.getProperties().size() == properties.size();
        assert product.getProperties().get("Display").equals("OLED");
        assert product.getProperties().get("Connectivity").equals("5G Support");
    }
}
