package be.uantwerpen.fti.se.imagineframe_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imf_tags")
public class Tag {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private Set<Product> products = new HashSet<>();

    public Tag(String name) {
        this.name = name;
    }

    public Set<String> getProductsName() {
        return products.stream().map(Product::getName).collect(Collectors.toSet());
    }

    public void addProduct(Product product) {
        this.products.add(product);
        product.addTag(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.removeTag(this);
    }
}
