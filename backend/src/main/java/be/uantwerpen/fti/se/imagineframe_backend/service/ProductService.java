package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProductAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public boolean doesProductExist(Long id) {
        return productRepository.existsById(id);
    }

    public void saveProduct(Product product) {
        try {
            productRepository.save(product);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("product", String.valueOf(product.getProductId()));
        }
    }

    public Product updateProductInformation(Product product, ProductAddDto productDto) {
        if (productDto.getName() != null) {
            product.setName(productDto.getName());
        }
        if (productDto.getDescription() != null) {
            product.setDescription(productDto.getDescription());
        }
        if (productDto.getImageUrl() != null) {
            String previousImageUrl = product.getImageUrl();
            product.setImageUrl(productDto.getImageUrl());

            if (previousImageUrl != null && !previousImageUrl.equals("placeholder.jpg")) {
                removeImage(previousImageUrl);
            }
        }
        if (productDto.getProperties() != null) {
            product.setProperties(productDto.getProperties());
        }

        return product;
    }

    public void removeImage(String imageUrl) {
        File file = new File("uploads/" + imageUrl);
        if (file.exists()) file.delete();
    }
}

