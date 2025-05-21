package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectJoinRequestRepository requestRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectJoinRequestRepository requestRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.userService = userService;
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public void saveProject(Project project) {
        try {
            projectRepository.save(project);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("project", String.valueOf(project.getId()));
        }
    }

    public Project updateProjectInformation(Project project, ProjectEditDto projectDto) {
        if (projectDto.getName() != null) {
            project.setName(projectDto.getName());
        }
        if (projectDto.getDescription() != null) {
            project.setDescription(projectDto.getDescription());
        }
        if (projectDto.getMediaUrl() != null) {
            project.setMediaUrl(projectDto.getMediaUrl());
        }
        if (projectDto.getStatus() != null) {
            project.setStatus(projectDto.getStatus());
        }

        if (projectDto.getUsers() != null) {
            Set<User> updatedUsers = new HashSet<>();
            for (UserGetDto userDto : projectDto.getUsers()) {
                updatedUsers.add(userRepository.findById(userDto.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));
            }
            Set<User> removedUsers = new HashSet<>(project.getUsers());
            removedUsers.removeAll(updatedUsers);
            for (User removedUser : removedUsers) {
                Optional<ProjectJoinRequest> request = requestRepository.findByUserIdAndProjectId(removedUser.getId(), project.getId());
                request.ifPresent(requestRepository::delete);
            }
            if (project.getOwner() != null && !updatedUsers.contains(project.getOwner())) {
                updatedUsers.add(project.getOwner());
            }
            project.setUsers(updatedUsers);
        }
        return project;
    }

    public List<Project> findAll() {

        return (List<Project>) projectRepository.findAll();
    }

    public List<Project> findFilteredProjects(ProjectStatus status, List<Long> userIds) {
        List<Project> allProjects = findAll();
        if (status != null) {
            allProjects = allProjects.stream().filter(project -> project.getStatus().equals(status)).collect(Collectors.toList());
        }
        if (userIds != null && !userIds.isEmpty()) {
            allProjects = allProjects.stream().filter(project -> project.getUsers().stream().anyMatch(user -> userIds.contains(user.getId()))).collect(Collectors.toList());
        }
        return allProjects;

    }

    public boolean isProjectOwner(Long projectId, User user) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(projectId)));
        return project.getOwner() != null && project.getOwner().getId() == user.getId();
    }

    public List<Project> findProjectsByUser(User user) {
        List<Project> allProjects = (List<Project>) projectRepository.findAll();
        return allProjects.stream().filter(project -> project.getUsers().contains(user)).collect(Collectors.toList());
    }

    public Set<User> getVisibleProjectMembers(Project project, User currentUser) {
        boolean hasProjectWritePrivilege;
        if (currentUser != null) {
            hasProjectWritePrivilege = currentUser.getGroups().stream().flatMap(g -> g.getPrivileges().stream()).anyMatch(p -> p.getName().equals("project_write"));
        } else {
            hasProjectWritePrivilege = false;
        }
        return project.getUsers().stream().filter(user -> {
            if (hasProjectWritePrivilege || user.getId() == currentUser.getId()) {
                return true;
            }
                    switch (user.getPrivacyLevel()) {
                        case PUBLIC:
                            return true;
                        case IMAGINEERS_ONLY:
                            return currentUser.getGroups().stream().anyMatch(g -> g.getName().equals("iMagineer"));
                        case PRIVATE:
                            return false;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toSet());
    }

    public List<Project> findPublicProjects() {
        List<Project> allProjects = (List<Project>) projectRepository.findAll();
        return allProjects.stream().filter(Project::isPublic).collect(Collectors.toList());
    }

    public Set<User> getPublicProjectMembers(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("project", String.valueOf(projectId)));
        if (!project.isPublic()) {
            return new HashSet<>();
        }
        return project.getUsers().stream().filter(user -> user.getPrivacyLevel() == PrivacyLevel.PUBLIC).collect(Collectors.toSet());
    }

    public int getPublicProjectMembersCount(Long projectId) {
        return getPublicProjectMembers(projectId).size();
    }
}
