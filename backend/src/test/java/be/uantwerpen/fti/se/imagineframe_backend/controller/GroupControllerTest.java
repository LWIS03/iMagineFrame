package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.repository.GroupRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @InjectMocks
    private ModelMapper modelMapper;

    @Autowired
    public GroupControllerTest(GroupService groupService) {
        this.groupService = groupService;
    }

    @BeforeEach
    public void setUp() {
        groupController = new GroupController(groupRepository, groupService, modelMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).setControllerAdvice(GlobalExceptionHandler.class).build();
    }

    @Test
    public void testGetGroups() throws Exception {
        // Arrange
        Group group1 = new Group("Group1");
        Group group2 = new Group("Group2");
        List<Group> groups = Arrays.asList(group1, group2);

        when(groupRepository.findAll()).thenReturn(groups);

        // Act & Assert
        mockMvc.perform(get("/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Group1"))
                .andExpect(jsonPath("$[1].name").value("Group2"));
    }

    @Test
    public void testGetGroupByID() throws Exception {
        // Arrange
        Group group = new Group("Group1");
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        // Act & Assert
        mockMvc.perform(get("/groups/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Group1"));
    }

    @Test
    public void testGetGroupByID_NotFound() throws Exception {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/groups/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteGroupByID() throws Exception {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.of(new Group()));

        // Act & Assert
        mockMvc.perform(delete("/groups/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(groupService, times(1)).deleteByID(1L);
    }

    @Test
    public void testDeleteGroupByID_NotFound() throws Exception {
        // Arrange
        when(groupRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/groups/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
