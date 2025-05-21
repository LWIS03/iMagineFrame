package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.Tag;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProductAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.TagRepository;
import be.uantwerpen.fti.se.imagineframe_backend.security.UrlSecurity;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProductService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/products")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Product Management", description = "API for managing products, including CRUD operations and tag management")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductController {
    private static final String DEFAULT_IMAGE = "placeholder.png";

    @Value("${base-url}")
    private String url;
    @Value("${files-path}")
    private String filesPath;

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final UrlSecurity urlSecurity;

    public ProductController(ProductRepository productRepository, ProductService productService, TagRepository tagRepository, FileStorageService fileStorageService, UserService userService, UrlSecurity urlSecurity) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.tagRepository = tagRepository;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.urlSecurity = urlSecurity;
    }

    @Operation(summary = "Get all products", description = "Retrieves the complete list of products in the system.")
    @GetMapping
    public Iterable<Product> getProducts() {
        return productRepository.findAll();
    }

    @Operation(summary = "Get product by name", description = "Retrieves a specific product by its name.")
    @GetMapping("/name/{name}")
    public ResponseEntity<Product> getProductByName(@PathVariable String name) {
        Optional<Product> product = productRepository.findByName(name);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            throw new EntityNotFoundException("product", name);
        }
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its ID.")
    @GetMapping("/id/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            throw new EntityNotFoundException("product", String.valueOf(id));
        }
    }

    @Operation(summary = "Add a new product", description = "Creates a new product with optional image upload.")
    @PostMapping("/add")
    public ResponseEntity<String> addProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {

        //convert json to dto
        ProductAddDto productAddDto = new ObjectMapper().readValue(productJson, ProductAddDto.class);
        Product newProduct = new Product();
        newProduct = productService.updateProductInformation(newProduct, productAddDto);

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            newProduct.setImageUrl(fileName);
        }

        productService.saveProduct(newProduct);
        return ResponseEntity.ok("Product added successfully");
    }

    @Operation(summary = "Delete product by ID", description = "Removes a product by its ID.")
    @DeleteMapping("/id/{id}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> deleteProductById(@PathVariable Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            throw new EntityNotFoundException("product", String.valueOf(id));
        }
        productRepository.deleteById(id);
        if (!product.get().getImageUrl().equals(DEFAULT_IMAGE)) {
            productService.removeImage(product.get().getImageUrl());
        }
        return ResponseEntity.ok(id + " deleted successfully");
    }

    @Operation(summary = "Add tag to product", description = "Associates an existing tag with a product.")
    @PutMapping("/{productId}/addTag/{tagId}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> addTagToProduct(@PathVariable Long productId, @PathVariable Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("product", String.valueOf(productId)));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("tag", String.valueOf(tagId)));

        product.addTag(tag);
        productRepository.save(product);
        return ResponseEntity.ok("Tag added successfully to product");
    }

    @Operation(summary = "Delete product by name", description = "Removes a product by its name.")
    @DeleteMapping("/name/{name}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> deleteProductByName(@PathVariable String name) {
        Optional<Product> product = productRepository.findByName(name);
        if (product.isEmpty()) {
            throw new EntityNotFoundException("product", name);
        }

        productRepository.deleteByName(name);
        return ResponseEntity.ok(name + " deleted successfully");
    }

    @Operation(summary = "Remove tag from product", description = "Disassociates a tag from a product.")
    @DeleteMapping("/{productId}/removeTag/{tagId}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> removeTagFromProduct(@PathVariable Long productId, @PathVariable Long tagId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("product", String.valueOf(productId)));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException("tag", String.valueOf(tagId)));

        if (!product.getTags().contains(tag)) {
            throw new EntityNotFoundException("tag in product", "Product ID: " + productId + ", Tag ID: " + tagId);
        }
        product.removeTag(tag);
        productRepository.save(product);
        return ResponseEntity.ok("Tag removed successfully from product");
    }

    @Operation(summary = "Get product image", description = "Retrieves the image associated with a product.")
    @GetMapping("/{productId}/image")
    public ResponseEntity<Resource> getProductImage(@PathVariable Long productId) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product", String.valueOf(productId)));

        String fileName = product.getImageUrl();
        Path path = Paths.get("uploads").resolve(fileName);
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Could not read file: " + fileName);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @Operation(summary = "Edit existing product", description = "Updates a product's information with optional image upload.")
    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<?> editProduct(
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("product", String.valueOf(id)));

        ProductAddDto productAddDto = new ObjectMapper().readValue(productJson, ProductAddDto.class);

        productService.updateProductInformation(existingProduct, productAddDto);

        if (productAddDto.getImageUrl() != null && !productAddDto.getImageUrl().isEmpty()) {
            existingProduct.setImageUrl(productAddDto.getImageUrl());
        } else if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(imageFile);
            existingProduct.setImageUrl(fileName);
        }


        if (productAddDto.getTagIds() != null && !productAddDto.getTagIds().isEmpty()) {
            existingProduct.getTags().clear();

            for (Long tag : productAddDto.getTagIds()) {
                Tag existingTag = tagRepository.findById(tag)
                        .orElseThrow(() -> new EntityNotFoundException("tag", String.valueOf(tag)));
                existingProduct.addTag(existingTag);
            }
        }

        productService.saveProduct(existingProduct);

        return ResponseEntity.ok("Product updated successfully");
    }

    @Operation(summary = "Get product stock report URL", description = "Generates a secure URL for accessing the product stock report.")
    @GetMapping("/report")
    @PreAuthorize("hasAuthority('product_manager')")
    public ResponseEntity<String> getProductStockReportUrl() throws NoSuchAlgorithmException {
        // Get user who is requesting the report
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = this.userService.findUser(loggedInUser);

        // Create url path
        String urlPath = "/api/files/report/" + urlSecurity.createUrlToken(me);
        return ResponseEntity.ok(urlPath);
    }
}
