package be.uantwerpen.fti.se.imagineframe_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
@Entity
@Table(name = "imf_products")
public class Product {
    private static final String DEFAULT_NAME = "default_name";
    private static final String DEFAULT_DESCRIPTION = "default_description";
    private static final Map<String, String> DEFAULT_PROPERTY = new HashMap<>();
    private static final String DEFAULT_IMAGE = "placeholder.png";

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long productId;

    @Column(updatable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(length = 255)
    private String imageUrl;

    /*
     * Here we create the table "product_tags" where we can see the correspondence between
     * products nad tags,
     */
    @ManyToMany
    @JoinTable(
            name = "imf_product_mtm_tag",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    /*  @JsonIgnore is used because at the time of doing a get request for all products there's
     *   a recursion call between products and tags, so this cuts the recursion call
     */
    @JsonIgnore
    private Set<Tag> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "imf_product_properties", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "property_key")
    @Column(name = "property_value")
    private Map<String, String> properties = new HashMap<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private Set<Batch> batches = new HashSet<>();

    public Product() {
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.imageUrl = DEFAULT_IMAGE;
    }

    public Product(String name, String description, Map<String, String> properties, String imageUrl) {
        this.name = name;
        this.description = description;
        this.properties = (properties != null) ? properties : DEFAULT_PROPERTY;
        this.imageUrl = (imageUrl != null) ? imageUrl : DEFAULT_IMAGE;
    }

    /* This function is used because when doing a GET request for all tags,
     *  it only prints TagId, TagName, and ProductName. Otherwise, it would print
     *  The whole products with all its information
     */
    public Set<String> getTagsName(){
        return tags.stream().map(Tag::getName).collect(Collectors.toSet());
    }

    public Set<Long> getBatchIds() {
        return batches.stream().map(Batch::getId).collect(Collectors.toSet());
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }
}
