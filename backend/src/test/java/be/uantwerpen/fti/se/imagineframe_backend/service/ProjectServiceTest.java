package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectJoinRequestRepository requestRepository;

    @Mock
    private UserService userService;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectService = new ProjectService(projectRepository, userRepository, requestRepository, userService);
    }

    /**
     * create a project and add user to it
     */
    @Test
    void testCreateProjectWithUser() {
        Project project = new Project();
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setStatus(ProjectStatus.PLANNING);
        project.setMediaUrl("http://test.com/image.jpg");
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        ProjectEditDto projectEditDto = new ProjectEditDto();
        projectEditDto.setName("Test Project");
        projectEditDto.setDescription("Test Description");
        projectEditDto.setStatus(ProjectStatus.PLANNING);
        Set<UserGetDto> users = new HashSet<>();
        UserGetDto userGetDto = new UserGetDto();
        userGetDto.setId(1L);
        users.add(userGetDto);
        projectEditDto.setUsers(users);
        Project updatedProject = projectService.updateProjectInformation(project, projectEditDto);
        projectService.saveProject(updatedProject);
        assertNotNull(updatedProject.getUsers());
        assertTrue(updatedProject.getUsers().contains(user));
        verify(userRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(project);
    }

    /**
     * Try to add non existing user to project
     */
    @Test
    void testCreateProjectWithNonExistentUser() {
        Project project = new Project();
        project.setName("Test Project");
        ProjectEditDto projectEditDto = new ProjectEditDto();
        projectEditDto.setName("Test Project");
        Set<UserGetDto> users = new HashSet<>();
        UserGetDto userGetDto = new UserGetDto();
        userGetDto.setId(900L); // id 900 non existent
        users.add(userGetDto);
        projectEditDto.setUsers(users);
        when(userRepository.findById(900L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            projectService.updateProjectInformation(project, projectEditDto);
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }
}