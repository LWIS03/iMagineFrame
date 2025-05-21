package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectJoinRequestAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectJoinRequestGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectJoinRequestService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects/requests")
@Tag(name = "Project Join Requests", description = "API for managing project join requests and membership")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectJoinRequestController {

    private final ProjectJoinRequestRepository requestRepository;
    private final ProjectJoinRequestService requestService;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ProjectRepository projectRepository;
    private final Logger logger = LoggerFactory.getLogger(ProjectJoinRequestController.class);

    public ProjectJoinRequestController(ProjectJoinRequestRepository requestRepository, ProjectJoinRequestService requestService, UserService userService, ModelMapper modelMapper, ProjectRepository projectRepository) {
        this.requestRepository = requestRepository;
        this.requestService = requestService;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.projectRepository = projectRepository;
    }

    @Operation(summary = "Get all join requests",
            description = "Retrieves all project join requests in the system.")
    @GetMapping
    @PreAuthorize("hasAuthority('project_write')")
    public List<ProjectJoinRequestGetDto> getAllRequests() {
        logger.info("GET: /projects/requests");
        return ((List<ProjectJoinRequest>) requestRepository.findAll()).stream().map(request -> modelMapper.map(request, ProjectJoinRequestGetDto.class)).collect(Collectors.toList());
    }

    @Operation(summary = "Get current user's requests",
            description = "Retrieves all join requests made by the authenticated user.")
    @GetMapping("/user")
    @PreAuthorize("hasAuthority('project_read')")
    public List<ProjectJoinRequestGetDto> getCurrentUserRequests() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        logger.info("GET: /projects/requests/user for user {}", currentUser.getId());
        return requestService.getUserRequests(currentUser.getId()).stream().map(request -> modelMapper.map(request, ProjectJoinRequestGetDto.class)).collect(Collectors.toList());
    }

    @Operation(summary = "Get requests for project",
            description = "Retrieves all join requests for a specific project.")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAuthority('project_write')")
    public List<ProjectJoinRequestGetDto> getProjectRequests(@PathVariable Long projectId) {
        logger.info("GET: /projects/requests/project/{}", projectId);
        return requestService.getProjectRequests(projectId).stream().map(request -> modelMapper.map(request, ProjectJoinRequestGetDto.class)).collect(Collectors.toList());
    }

    @Operation(summary = "Create join request",
            description = "Creates a new request to join a project.")
    @PostMapping("/new")
    @PreAuthorize("hasAuthority('project_read')")
    public ResponseEntity<ProjectJoinRequestGetDto> createRequest(@RequestBody ProjectJoinRequestAddDto requestDto) {
        logger.info("POST: /projects/requests/new");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        ProjectJoinRequest request = requestService.saveRequest(currentUser, requestDto.getProjectId());
        return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(request, ProjectJoinRequestGetDto.class));
    }

    @Operation(summary = "Accept join request",
            description = "Approves a pending join request.")
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id) {
        logger.info("POST: /projects/requests/{}/accept", id);
        requestService.acceptRequest(id);
        return ResponseEntity.ok("request accepted");
    }

    @Operation(summary = "Decline join request",
            description = "Rejects a pending join request.")
    @PostMapping("/{id}/decline")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> declineRequest(@PathVariable Long id) {
        logger.info("POST: /projects/requests/{}/decline", id);
        requestService.declineRequest(id);
        return ResponseEntity.ok("request declined");
    }

    @Operation(summary = "Delete join request",
            description = "Removes a join request created by the current user.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project_read')")
    public ResponseEntity<String> deleteRequest(@PathVariable Long id) {
        logger.info("DELETE: /projects/requests/{}", id);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        requestService.deleteRequest(id, currentUser);
        return ResponseEntity.ok("request deleted");
    }

    @Operation(summary = "Delete all declined requests",
            description = "Removes all join requests that were declined.")
    @DeleteMapping("/declined")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> deleteAllDeclinedRequests() {
        logger.info("DELETE: /projects/requests/declined");
        long count = requestService.deleteAllDeclinedRequests();
        return ResponseEntity.ok("deleted " + count + " declined requests");
    }

    @Operation(summary = "Delete all accepted requests",
            description = "Removes all join requests that were accepted.")
    @DeleteMapping("/accepted")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> deleteAllAcceptedRequests() {
        logger.info("DELETE: /projects/requests/accepted");
        long count = requestService.deleteAllAcceptedRequests();
        return ResponseEntity.ok("deleted " + count + " accepted requests");
    }

    @Operation(summary = "Delete user-project request",
            description = "Removes the current user's request for a specific project.")
    @DeleteMapping("/user-project/{projectId}")
    @PreAuthorize("hasAuthority('project_read')")
    public ResponseEntity<String> deleteUserProjectRequest(@PathVariable Long projectId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        Optional<ProjectJoinRequest> request = requestRepository.findByUserIdAndProjectId(currentUser.getId(), projectId);
        if (request.isPresent()) {
            requestRepository.delete(request.get());
            return ResponseEntity.ok("request deleted");
        }
        return ResponseEntity.ok("no request found to delete");
    }

    @Operation(summary = "Delete specific request",
            description = "Allows administrators to remove any join request.")
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> deleteIndividualRequest(@PathVariable Long id) {
        logger.info("DELETE: /projects/requests/admin/{}", id);
        ProjectJoinRequest request = requestRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ProjectJoinRequest", String.valueOf(id)));
        requestRepository.deleteById(id);
        return ResponseEntity.ok("request deleted");
    }

    @Operation(summary = "Leave project",
            description = "Removes the current user from a project they are a member of.")
    @PostMapping("/leave-project/{projectId}")
    @PreAuthorize("hasAuthority('project_read')")
    public ResponseEntity<String> leaveProject(@PathVariable Long projectId) {
        logger.info("POST: /projects/requests/leave-project/{}", projectId);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(projectId)));
        if (project.getOwner() != null && project.getOwner().getId() == currentUser.getId()) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Project creator cannot leave the project");
        }
        if (project.getUsers().contains(currentUser)) {
            project.getUsers().remove(currentUser);
            projectRepository.save(project);
            Optional<ProjectJoinRequest> request = requestRepository.findByUserIdAndProjectId(currentUser.getId(), projectId);
            request.ifPresent(requestRepository::delete);
            return ResponseEntity.ok("Left project successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is not a member of this project");
        }
    }
}