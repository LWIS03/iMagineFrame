package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.EventParticipation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventParticipationRepository extends CrudRepository<EventParticipation, Long> {
    Optional<EventParticipation> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventParticipation> findByEventId(Long eventId);
    List<EventParticipation> findByUserId(Long userId);
}
