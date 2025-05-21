package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.CalendarToken;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CalendarTokenRepository extends CrudRepository<CalendarToken, String> {
    Optional<CalendarToken> findByToken(String token);
    void deleteByUser(User user);
}
