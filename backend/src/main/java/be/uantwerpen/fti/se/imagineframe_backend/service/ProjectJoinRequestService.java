package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectJoinRequestService {

    private final ProjectJoinRequestRepository requestRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectJoinRequestService(ProjectJoinRequestRepository requestRepository,
                                     ProjectRepository projectRepository,
                                     UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectJoinRequest saveRequest(User user, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(projectId)));

        //we check her if the user is alraedy a member of the project
        if (project.getUsers().contains(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"the user is already a member of the project");
        }

        //we check if the request already exist
        Optional<ProjectJoinRequest> existingRequest = requestRepository.findByUserIdAndProjectId(user.getId(), projectId);
        if (existingRequest.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "the request to join this project already exists");
        }
        ProjectJoinRequest request = new ProjectJoinRequest(user, project);
        try {
            return requestRepository.save(request);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("ProjectJoinRequest", "user: " + user.getId() + ", project: " + projectId);
        }
    }

    public void acceptRequest(Long requestId) {
        ProjectJoinRequest request = requestRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("ProjectJoinRequest", String.valueOf(requestId)));
        request.setAccepted(true);
        Project project = request.getProject();
        User user = request.getUser();
        project.getUsers().add(user);
        projectRepository.save(project);
        requestRepository.save(request);
    }

    public void declineRequest(Long requestId) {
        ProjectJoinRequest request = requestRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("ProjectJoinRequest", String.valueOf(requestId)));
        request.setAccepted(false);
        requestRepository.save(request);
    }

    public void deleteRequest(Long requestId, User user) {
        ProjectJoinRequest request = requestRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("ProjectJoinRequest", String.valueOf(requestId)));
        if (!(request.getUser().getId() == user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "you can only delete your own requests");
        }
        requestRepository.deleteById(requestId);
    }

    public List<ProjectJoinRequest> getUserRequests(Long userId) {
        return requestRepository.findByUserId(userId);
    }

    public List<ProjectJoinRequest> getProjectRequests(Long projectId) {
        return requestRepository.findByProjectId(projectId);
    }

    @Transactional
    public Long deleteAllDeclinedRequests() {
        try {
            return requestRepository.deleteByAccepted(false);
        } catch (OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("ProjectJoinRequest", "declined");
        }
    }

    @Transactional
    public Long deleteAllAcceptedRequests() {
        try {
            return requestRepository.deleteByAccepted(true);
        } catch (OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("ProjectJoinRequest", "accepted");
        }
    }
}