package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@Tag(name = "Project Management", description = "API for managing projects, including CRUD operations and member management")
@SecurityRequirement(name = "Bearer Authentication")
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelmapper;
    private final UserService userService;
    @Value("${base-url}")
    private String url;
    @Value("${files-path}")
    private String filesPath;

    public ProjectController(ProjectRepository projectRepository, ProjectService projectService, FileStorageService fileStorageService, UserRepository userRepository, ModelMapper modelmapper, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.fileStorageService = fileStorageService;
        this.userRepository = userRepository;
        this.modelmapper = modelmapper;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Operation(summary = "Get all projects", description = "Retrieves the complete list of projects.")
    @GetMapping
    public List<Project> getProjects() {
        return projectService.findAll();
    }

    @Operation(summary = "Get project by ID",
            description = "Retrieves a specific project by its ID.")
    @GetMapping("/{id}")
    public Project getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(id)));
    }

    @Operation(summary = "Get filtered projects",
            description = "Retrieves projects filtered by status and/or user IDs.")
    @GetMapping("/filter")
    public List<Project> getFilteredProjects(@RequestParam(required = false) ProjectStatus status, @RequestParam(required = false) List<Long> userId) {
        return projectService.findFilteredProjects(status, userId);
    }

    @Operation(summary = "Delete a project",
            description = "Removes a project from the system.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return ResponseEntity.ok(id + " deleted successfully");
        } else {
            throw new EntityNotFoundException("project", String.valueOf(id));
        }
    }

    @Operation(summary = "Create a new project", description = "Creates a new project with optional media upload.")
    @PostMapping("/new")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<Project> createProject(@RequestPart("project") String projectJson, @RequestPart(value = "media", required = false) MultipartFile mediaFile) throws Exception {
        ProjectEditDto projectDto = objectMapper.readValue(projectJson, ProjectEditDto.class);
        Project newProject = new Project();
        newProject = projectService.updateProjectInformation(newProject, projectDto);

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        newProject.setOwner(currentUser);

        newProject.getUsers().add(currentUser);

        if (mediaFile != null && !mediaFile.isEmpty()) {
            String filename = fileStorageService.storeFile(mediaFile);
            newProject.setMediaUrl(url + filesPath + filename);
        }
        projectService.saveProject(newProject);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProject);
    }

    @Operation(summary = "Update a project",
            description = "Updates an existing project's information with optional media upload.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('project_write')")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestPart("project") String projectJson, @RequestPart(value = "media", required = false) MultipartFile mediaFile
    ) throws Exception {
        ProjectEditDto projectDto = objectMapper.readValue(projectJson, ProjectEditDto.class);
        Project existingProject = projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(id)));
        Project updatedProject = projectService.updateProjectInformation(existingProject, projectDto);
        if (mediaFile != null && !mediaFile.isEmpty()) {
            String filename = fileStorageService.storeFile(mediaFile);
            updatedProject.setMediaUrl(url + filesPath + filename);
        }
        projectService.saveProject(updatedProject);
        return ResponseEntity.ok(updatedProject);
    }

    @Operation(summary = "Get potential project contributors",
            description = "Retrieves a list of all users who can be added to projects.")
    @GetMapping("/contributors")
    @PreAuthorize("hasAuthority('project_read')")
    public List<UserGetDto> getAllPotentialProjectContributors() {
        return ((List<User>) userRepository.findAll()).stream().map(user -> modelmapper.map(user, UserGetDto.class)).toList();
    }

    @Operation(summary = "Get public projects",
            description = "Retrieves all publicly visible projects.")
    @GetMapping("/public")
    public List<Project> getPublicProjects() {
        return projectService.findPublicProjects();
    }

    @Operation(summary = "Get public project members",
            description = "Retrieves members of a public project who have public profiles.")
    @GetMapping("/{id}/public-members")
    public ResponseEntity<List<UserGetDto>> getPublicProjectMembers(@PathVariable Long id) {
        Set<User> publicMembers = projectService.getPublicProjectMembers(id);
        List<UserGetDto> dto = publicMembers.stream()
                .map(user -> modelmapper.map(user, UserGetDto.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get public members count",
            description = "Counts the number of public members in a project.")
    @GetMapping("/{id}/public-members/count")
    public ResponseEntity<Integer> getPublicMembersCount(@PathVariable Long id) {
        int count = projectService.getPublicProjectMembersCount(id);
        return ResponseEntity.ok(count);
    }

    // returns all the projects that an user is participating in
    @Operation(summary = "Get current user's projects",
            description = "Retrieves all projects the authenticated user is participating in.")
    @GetMapping("/my-projects")
    @PreAuthorize("hasAuthority('project_read')")
    public List<ProjectGetDto> getCurrentUserProjects() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        return projectService.findProjectsByUser(currentUser).stream().map(project -> modelmapper.map(project, ProjectGetDto.class)).collect(Collectors.toList());
    }

    @Operation(summary = "Get visible project users",
            description = "Retrieves users in a project visible to the current user based on privacy settings.")
    @GetMapping("/{id}/visible-users")
    @PreAuthorize("hasAuthority('project_read')")
    public List<UserGetDto> getVisibleProjectUsers(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        Project project = projectRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(id)));
        Set<User> visibleUsers = projectService.getVisibleProjectMembers(project,currentUser);
        return visibleUsers.stream().map(user -> modelmapper.map(user, UserGetDto.class)).collect(Collectors.toList());
    }



}
