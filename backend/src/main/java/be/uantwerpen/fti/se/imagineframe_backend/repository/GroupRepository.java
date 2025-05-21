package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends CrudRepository<Group, Long> {
    Optional<Group> findByName(String name);
}
