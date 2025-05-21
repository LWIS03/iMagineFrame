package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectControllerImageTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectController projectController;

    private User testUser;
    private Project mockProject;
    private ProjectEditDto projectDto;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("1");

        testUser = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(testUser, 1L);
        when(userService.findUser("1")).thenReturn(testUser);

        projectDto = new ProjectEditDto();
        projectDto.setName("Test Project");
        projectDto.setDescription("Test Description");
        projectDto.setStatus(ProjectStatus.PLANNING);

        mockProject = new Project();
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockProject, 1L);
        mockProject.setName("Test Project");
        mockProject.setDescription("Test Description");
        mockProject.setStatus(ProjectStatus.PLANNING);
        mockProject.setOwner(testUser);

        when(projectService.updateProjectInformation(any(Project.class), any(ProjectEditDto.class))).thenReturn(mockProject);
    }

    @Test
    void createProject_JpgImage() throws Exception {
        MockMultipartFile jpg = new MockMultipartFile("media", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());
        String projectJson = new ObjectMapper().writeValueAsString(projectDto);
        when(fileStorageService.storeFile(any())).thenReturn("test.jpg");

        ResponseEntity<Project> response = projectController.createProject(projectJson, jpg);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(fileStorageService).storeFile(any());
        verify(projectService).saveProject(any());
    }

    @Test
    void createProject_PdfFile() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile("media", "document.pdf", "application/pdf", "PDF content".getBytes());
        String projectJson = new ObjectMapper().writeValueAsString(projectDto);
        when(fileStorageService.storeFile(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only images are allowed"));

        assertThrows(ResponseStatusException.class, () -> projectController.createProject(projectJson, pdf));

        verify(fileStorageService).storeFile(any());
        verify(projectService, never()).saveProject(any());
    }

    @Test
    void createProject_NoImage() throws Exception {
        String projectJson = new ObjectMapper().writeValueAsString(projectDto);

        ResponseEntity<Project> response = projectController.createProject(projectJson, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(fileStorageService, never()).storeFile(any());
        verify(projectService).saveProject(any());
    }
}