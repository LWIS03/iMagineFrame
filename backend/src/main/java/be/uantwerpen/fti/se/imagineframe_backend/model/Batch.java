package be.uantwerpen.fti.se.imagineframe_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imf_batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    private double unitPrice;

    private LocalDateTime addedDate = LocalDateTime.now();

    private LocalDateTime expirationDate;

    public Batch(Product product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Batch(Product product, int quantity, double unitPrice, LocalDateTime expirationDate) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.expirationDate = expirationDate;
    }


    public long getProductId() {
        return product.getProductId();
    }
}
