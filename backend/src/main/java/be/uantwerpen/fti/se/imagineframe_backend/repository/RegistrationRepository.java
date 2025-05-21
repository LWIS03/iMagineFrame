package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends CrudRepository<Registration, Long> {
    // Deletes all registration based on the value of the "accepted" boolean. If registration.accepted matches the value given in this function, the registration will be removed.
    Long deleteByAccepted(Boolean accepted);
}
