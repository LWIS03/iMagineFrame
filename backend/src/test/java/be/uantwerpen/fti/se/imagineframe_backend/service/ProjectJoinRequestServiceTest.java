package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectJoinRequestServiceTest {

    @Mock
    private ProjectJoinRequestRepository requestRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectJoinRequestService requestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveRequest() {
        User user = new User();
        user.setEmail("test@example.com");
        Project project = new Project();
        project.setUsers(new HashSet<>());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(requestRepository.findByUserIdAndProjectId(any(), any())).thenReturn(Optional.empty());
        when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ProjectJoinRequest result = requestService.saveRequest(user, 1L);
        assertNotNull(result);
        verify(requestRepository, times(1)).save(any());
    }

    @Test
    void testSaveRequest_UserAlreadyMember() {
        User user = new User();
        Project project = new Project();
        project.setUsers(new HashSet<>());
        project.getUsers().add(user);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        assertThrows(ResponseStatusException.class, () -> {requestService.saveRequest(user, 1L);});
    }

    @Test
    void testSaveRequest_RequestAlreadyExists() {
        User user = new User();
        Project project = new Project();
        project.setUsers(new HashSet<>());
        ProjectJoinRequest existingRequest = new ProjectJoinRequest();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(requestRepository.findByUserIdAndProjectId(any(), any())).thenReturn(Optional.of(existingRequest));
        assertThrows(ResponseStatusException.class, () -> {requestService.saveRequest(user, 1L);});
    }

    @Test
    void testAcceptRequest() {
        ProjectJoinRequest request = new ProjectJoinRequest();
        Project project = new Project();
        project.setUsers(new HashSet<>());
        User user = new User();
        request.setProject(project);
        request.setUser(user);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.acceptRequest(1L);
        assertTrue(request.getAccepted());
        verify(projectRepository, times(1)).save(project);
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void testDeclineRequest() {
        ProjectJoinRequest request = new ProjectJoinRequest();
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.declineRequest(1L);
        assertFalse(request.getAccepted());
        verify(requestRepository, times(1)).save(request);
    }

    @Test
    void testDeleteRequest() {
        ProjectJoinRequest request = new ProjectJoinRequest();
        User user = new User();
        request.setUser(user);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.deleteRequest(1L, user);
        verify(requestRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteRequest_NotOwner() {
        ProjectJoinRequest request = new ProjectJoinRequest();
        User requestUser = mock(User.class);
        User otherUser = mock(User.class);
        when(requestUser.getId()).thenReturn(1L);
        when(otherUser.getId()).thenReturn(2L);
        request.setUser(requestUser);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        assertThrows(ResponseStatusException.class, () -> {requestService.deleteRequest(1L, otherUser);});
    }

    @Test
    void testDeleteRequest_AlreadyProcessed() {
        ProjectJoinRequest request = new ProjectJoinRequest();
        User user = new User();
        request.setUser(user);
        request.setAccepted(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        requestService.deleteRequest(1L, user);
        verify(requestRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAllDeclinedRequests() {
        when(requestRepository.deleteByAccepted(false)).thenReturn(5L);
        Long result = requestService.deleteAllDeclinedRequests();
        assertEquals(5L, result);
        verify(requestRepository, times(1)).deleteByAccepted(false);
    }

    @Test
    void testDeleteAllAcceptedRequests() {
        when(requestRepository.deleteByAccepted(true)).thenReturn(3L);
        Long result = requestService.deleteAllAcceptedRequests();
        assertEquals(3L, result);
        verify(requestRepository, times(1)).deleteByAccepted(true);
    }
}