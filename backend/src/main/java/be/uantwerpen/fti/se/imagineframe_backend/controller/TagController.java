package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityWithTagNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.Tag;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.TagAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.TagRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag Management", description = "API for managing product tags")
@SecurityRequirement(name = "Bearer Authentication")
public class TagController {
    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Operation(summary = "Get all tags",
            description = "Retrieves the complete list of product tags.")
    @GetMapping
    public Iterable<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Operation(summary = "Get products by tag ID",
            description = "Retrieves all products associated with a specific tag.")
    @GetMapping("/id/{id}")
    public ResponseEntity<Iterable<Product>> returnProductsWithTagId(@PathVariable Long id) {
        Optional<Tag> tag = tagRepository.findById(id);
        if (tag.isPresent()) {
            Iterable<Product> productList = tag.get().getProducts();
            return ResponseEntity.ok(productList);
        }
        throw new EntityWithTagNotFoundException("product", String.valueOf(id));
    }

    @Operation(summary = "Create a new tag",
            description = "Creates a new product tag with unique name validation.")
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('product_write')")
    public ResponseEntity<String> AddTag(@RequestBody TagAddDto tag) {
        Optional<Tag> exisiting_tag = tagRepository.findByName(tag.getName());
        if (exisiting_tag.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Tag with name " + tag.getName() + " already exists");
        }
        Tag new_tag = new Tag(tag.getName());
        tagRepository.save(new_tag);
        return ResponseEntity.status(HttpStatus.CREATED).body("Tag " + tag.getName() + " added successfully");
    }
}
