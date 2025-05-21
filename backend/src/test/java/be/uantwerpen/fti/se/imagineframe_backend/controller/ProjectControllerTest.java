package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectControllerTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProjects() {
        Project project1 = new Project();
        project1.setName("project 1");
        Project project2 = new Project();
        project2.setName("project 2");
        List<Project> projectList = Arrays.asList(project1, project2);
        when(projectService.findAll()).thenReturn(projectList);
        List<Project> result = projectController.getProjects();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("project 1", result.get(0).getName());
        assertEquals("project 2", result.get(1).getName());
        verify(projectService, times(1)).findAll();
    }

    @Test
    void testGetProjectById() throws NoSuchFieldException, IllegalAccessException {
        Project project = new Project();
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(project, 1L);
        project.setName("test project");
        project.setDescription("test description");
        project.setStatus(ProjectStatus.PLANNING);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Project result = projectController.getProjectById(1L);
        assertNotNull(result);
        assertEquals("test project", result.getName());
        assertEquals("test description", result.getDescription());
        assertEquals(ProjectStatus.PLANNING, result.getStatus());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProjectById_NotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            projectController.getProjectById(999L);
        });
        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    void testDeleteProject() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        ResponseEntity<String> response = projectController.deleteProject(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("1 deleted successfully", response.getBody());
        verify(projectRepository, times(1)).existsById(1L);
        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProject_NotFound() {
        when(projectRepository.existsById(999L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> {
            projectController.deleteProject(999L);
        });
        verify(projectRepository, times(1)).existsById(999L);
        verify(projectRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetProjectContributors() throws NoSuchFieldException, IllegalAccessException {
        User user1 = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(user1, 1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setUsername("johndoe");
        user1.setEmail("john@ua.com");
        User user2 = new User();
        userIdField.set(user2, 2L);
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setUsername("janedoe");
        user2.setEmail("jane@ua.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(modelMapper.map(eq(user1), eq(UserGetDto.class))).thenAnswer(invocation -> {
            UserGetDto dto = new UserGetDto();
            dto.setId(user1.getId());
            dto.setFirstName(user1.getFirstName());
            dto.setLastName(user1.getLastName());
            dto.setUsername(user1.getUsername());
            dto.setEmail(user1.getEmail());
            return dto;
        });
        when(modelMapper.map(eq(user2), eq(UserGetDto.class))).thenAnswer(invocation -> {
            UserGetDto dto = new UserGetDto();
            dto.setId(user2.getId());
            dto.setFirstName(user2.getFirstName());
            dto.setLastName(user2.getLastName());
            dto.setUsername(user2.getUsername());
            dto.setEmail(user2.getEmail());
            return dto;
        });
        List<UserGetDto> result = projectController.getAllPotentialProjectContributors();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetProjectByIdWithContributors() throws NoSuchFieldException, IllegalAccessException {
        Project project = new Project();
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(project, 1L);
        project.setName("project w contributors");
        project.setStatus(ProjectStatus.IN_PROGRESS);
        Set<User> contributors = new HashSet<>();
        User user1 = new User();
        User user2 = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(user1,1L);
        userIdField.set(user2,2L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setUsername("johndoe");
        user1.setEmail("john@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Doe");
        user2.setUsername("janedoe");
        user2.setEmail("jane@example.com");
        contributors.add(user1);
        contributors.add(user2);
        project.setUsers(contributors);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Project result = projectController.getProjectById(1L);
        assertNotNull(result);
        assertNotNull(result.getUsers());
        assertEquals(2, result.getUsers().size());

        boolean foundJohn = false;
        boolean foundJane = false;
        for (User user : result.getUsers()) {
            if (user.getUsername().equals("johndoe")) {
                foundJohn = true;
                assertEquals("John", user.getFirstName());
                assertEquals("Doe", user.getLastName());
            } else if (user.getUsername().equals("janedoe")) {
                foundJane = true;
                assertEquals("Jane", user.getFirstName());
                assertEquals("Doe", user.getLastName());
            }
        }
        assertTrue(foundJohn,"");
        assertTrue(foundJane, "");
    }

    @Test
    void testGetProjectByIdWithMedia() throws NoSuchFieldException, IllegalAccessException {
        Project project = new Project();
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(project, 1L);
        project.setName("Project1");
        project.setDescription("Testtesttest");
        project.setMediaUrl("http://example.com/testtest.jpg");
        project.setStatus(ProjectStatus.COMPLETED);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Project result = projectController.getProjectById(1L);
        assertNotNull(result);
        assertEquals("http://example.com/testtest.jpg", result.getMediaUrl());
    }

    @Test
    void testGetProjectByIdCompleteProject() throws NoSuchFieldException, IllegalAccessException {
        Project project = new Project();
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(project, 1L);
        project.setName("Project2");
        project.setDescription("description");
        project.setMediaUrl("http://example.com/testtest.jpg");
        project.setStatus(ProjectStatus.IN_PROGRESS);

        Set<User> users = new HashSet<>();
        User user = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(user, 1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("testuser");
        user.setEmail("test@ua.com");
        users.add(user);
        project.setUsers(users);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        Project result = projectController.getProjectById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Project2", result.getName());
        assertEquals("description", result.getDescription());
        assertEquals("http://example.com/testtest.jpg", result.getMediaUrl());
        assertEquals(ProjectStatus.IN_PROGRESS, result.getStatus());
        assertNotNull(result.getUsers());
        assertEquals(1, result.getUsers().size());
    }
}