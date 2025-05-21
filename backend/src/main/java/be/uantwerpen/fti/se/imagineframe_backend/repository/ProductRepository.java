package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends CrudRepository<Product, Long> {
    Optional<Product> findByName(String name);

    @Transactional
    void deleteByName(String name);
}
