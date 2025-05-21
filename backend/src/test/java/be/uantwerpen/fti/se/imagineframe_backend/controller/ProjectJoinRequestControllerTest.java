package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.ProjectJoinRequest;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProjectJoinRequestGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectJoinRequestService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectJoinRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private ProjectJoinRequestController requestController;

    @BeforeEach
    public void setUp() {
        requestController = new ProjectJoinRequestController(requestRepository, requestService, userService, modelMapper,projectRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(requestController).build();
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testGetAllRequests() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        Project project = new Project();
        project.setName("Test Project");
        ProjectJoinRequest request1 = new ProjectJoinRequest();
        request1.setUser(user);
        request1.setProject(project);
        ProjectJoinRequest request2 = new ProjectJoinRequest();
        request2.setUser(user);
        request2.setProject(project);
        List<ProjectJoinRequest> requests = Arrays.asList(request1, request2);
        when(requestRepository.findAll()).thenReturn(requests);
        mockMvc.perform(get("/projects/requests").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "logon", username = "testuser@example.com")
    public void testGetCurrentUserRequests() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        Project project = new Project();
        project.setName("Test Project");

        ProjectJoinRequest request = new ProjectJoinRequest();
        request.setUser(user);
        request.setProject(project);

        List<ProjectJoinRequest> requests = Arrays.asList(request);

        when(userService.findUser("testuser@example.com")).thenReturn(user);
        when(requestService.getUserRequests(anyLong())).thenReturn(requests);

        mockMvc.perform(get("/projects/requests/user").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testGetProjectRequests() throws Exception {
        User user = new User();
        user.setUsername("testuser");

        Project project = new Project();
        project.setName("Test Project");

        ProjectJoinRequest request = new ProjectJoinRequest();
        request.setUser(user);
        request.setProject(project);

        List<ProjectJoinRequest> requests = Arrays.asList(request);

        when(requestService.getProjectRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/projects/requests/project/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testAcceptRequest() throws Exception {
        mockMvc.perform(post("/projects/requests/1/accept").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("request accepted"));

        verify(requestService, times(1)).acceptRequest(1L);
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testDeclineRequest() throws Exception {
        mockMvc.perform(post("/projects/requests/1/decline").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("request declined"));

        verify(requestService, times(1)).declineRequest(1L);
    }

    @Test
    @WithMockUser(authorities = "logon", username = "testuser@example.com")
    public void testDeleteRequest() throws Exception {
        User user = new User();
        when(userService.findUser("testuser@example.com")).thenReturn(user);

        mockMvc.perform(delete("/projects/requests/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("request deleted"));

        verify(requestService, times(1)).deleteRequest(1L, user);
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testDeleteAllDeclinedRequests() throws Exception {
        when(requestService.deleteAllDeclinedRequests()).thenReturn(5L);

        mockMvc.perform(delete("/projects/requests/declined").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("deleted 5 declined requests"));
    }

    @Test
    @WithMockUser(authorities = "project_write")
    public void testDeleteAllAcceptedRequests() throws Exception {
        when(requestService.deleteAllAcceptedRequests()).thenReturn(3L);

        mockMvc.perform(delete("/projects/requests/accepted").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(content().string("deleted 3 accepted requests"));
    }
}