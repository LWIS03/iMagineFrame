package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.Batch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchRepository extends CrudRepository<Batch, Long> {

}
