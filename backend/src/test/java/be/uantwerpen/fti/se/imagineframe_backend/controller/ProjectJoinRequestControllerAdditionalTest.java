package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectJoinRequestService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectJoinRequestControllerAdditionalTest {

    @Mock
    private ProjectJoinRequestRepository requestRepository;

    @Mock
    private ProjectJoinRequestService requestService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectJoinRequestController controller;

    private User testUser;
    private Project testProject;
    private ProjectJoinRequest testRequest;

    @BeforeEach
    void setUp() throws Exception {
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
        testUser.setUsername("testuser");
        testProject = new Project();
        java.lang.reflect.Field projectIdField = Project.class.getDeclaredField("id");
        projectIdField.setAccessible(true);
        projectIdField.set(testProject, 1L);
        testProject.setName("Test Project");
        testProject.setUsers(new HashSet<>());
        testRequest = new ProjectJoinRequest();
        testRequest.setUser(testUser);
        testRequest.setProject(testProject);
        when(userService.findUser("1")).thenReturn(testUser);
    }

    @Test
    void testLeaveProject_Success() {
        testProject.getUsers().add(testUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(requestRepository.findByUserIdAndProjectId(1L, 1L)).thenReturn(Optional.of(testRequest));
        ResponseEntity<String> response = controller.leaveProject(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Left project successfully", response.getBody());
        assertFalse(testProject.getUsers().contains(testUser));
        verify(projectRepository, times(1)).save(testProject);
        verify(requestRepository, times(1)).delete(testRequest);
    }

    @Test
    void testLeaveProject_NotMember() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        ResponseEntity<String> response = controller.leaveProject(1L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User is not a member of this project", response.getBody());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void testLeaveProject_ProjectOwner() throws Exception {
        testProject.setOwner(testUser);
        testProject.getUsers().add(testUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        ResponseEntity<String> response = controller.leaveProject(1L);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Project creator cannot leave the project", response.getBody());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void testDeleteIndividualRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        ResponseEntity<String> response = controller.deleteIndividualRequest(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("request deleted", response.getBody());
        verify(requestRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteIndividualRequest_NotFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {controller.deleteIndividualRequest(1L);});
        verify(requestRepository, never()).deleteById(anyLong());
    }
}