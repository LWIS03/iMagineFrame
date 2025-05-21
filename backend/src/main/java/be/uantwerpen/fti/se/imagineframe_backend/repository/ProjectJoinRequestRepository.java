package be.uantwerpen.fti.se.imagineframe_backend.repository;

import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectJoinRequestRepository extends CrudRepository<ProjectJoinRequest, Long> {
    List<ProjectJoinRequest> findByUserId(Long userId);
    List<ProjectJoinRequest> findByProjectId(Long projectId);
    Optional<ProjectJoinRequest> findByUserIdAndProjectId(Long userId, Long projectId);
    Long deleteByAccepted(Boolean accepted);
}