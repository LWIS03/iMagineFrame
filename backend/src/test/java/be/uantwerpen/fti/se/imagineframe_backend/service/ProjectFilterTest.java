package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.controller.ProjectController;
import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ProjectFilterTest {

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

    @Mock
    private ProjectJoinRequestRepository requestRepository;

    @Mock
    private UserService userService;

    private ProjectController projectController;
    private List<Project> testProjects;
    private User user1, user2, user3;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        projectController = new ProjectController(projectRepository, projectService, fileStorageService, userRepository, modelMapper, userService);

        user1 = Mockito.mock(User.class);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        when(user1.getId()).thenReturn(1L);

        user2 = Mockito.mock(User.class);
        user2.setFirstName("A");
        user2.setLastName("B");
        when(user2.getId()).thenReturn(2L);

        user3 = Mockito.mock(User.class);
        user3.setFirstName("C");
        user3.setLastName("D");
        when(user3.getId()).thenReturn(3L);

        testProjects = new ArrayList<>();

        Project project1 = new Project();
        project1.setName("test1");
        project1.setDescription("testtest");
        project1.setStatus(ProjectStatus.PLANNING);
        Set<User> users1 = new HashSet<>();
        users1.add(user1);
        project1.setUsers(users1);

        Project project2 = new Project();
        project2.setName("test2");
        project2.setDescription("testtest");
        project2.setStatus(ProjectStatus.IN_PROGRESS);
        Set<User> users2 = new HashSet<>();
        users2.add(user2);
        project2.setUsers(users2);

        Project project3 = new Project();
        project3.setName("test3");
        project3.setDescription("testtest");
        project3.setStatus(ProjectStatus.COMPLETED);
        Set<User> users3 = new HashSet<>();
        users3.add(user1);
        users3.add(user3);
        project3.setUsers(users3);

        Project project4 = new Project();
        project4.setName("test4");
        project4.setDescription("testtest");
        project4.setStatus(ProjectStatus.ON_HOLD);
        Set<User> users4 = new HashSet<>();
        users4.add(user2);
        users4.add(user3);
        project4.setUsers(users4);

        Project project5 = new Project();
        project5.setName("test5");
        project5.setDescription("testtest");
        project5.setStatus(ProjectStatus.CANCELLED);
        project5.setUsers(new HashSet<>());

        testProjects.add(project1);
        testProjects.add(project2);
        testProjects.add(project3);
        testProjects.add(project4);
        testProjects.add(project5);

        when(projectService.findAll()).thenReturn(testProjects);
    }

    @Test
    void testFilterProjectsByStatus() {
        List<Project> planningProjects = Collections.singletonList(testProjects.get(0));
        when(projectService.findFilteredProjects(ProjectStatus.PLANNING, null)).thenReturn(planningProjects);
        List<Project> result = projectController.getFilteredProjects(ProjectStatus.PLANNING, null);
        assertEquals(1, result.size());
        assertEquals("test1", result.get(0).getName());
        assertEquals(ProjectStatus.PLANNING, result.get(0).getStatus());

        List<Project> inProgressProjects = Collections.singletonList(testProjects.get(1));
        when(projectService.findFilteredProjects(ProjectStatus.IN_PROGRESS, null)).thenReturn(inProgressProjects);
        result = projectController.getFilteredProjects(ProjectStatus.IN_PROGRESS, null);
        assertEquals(1, result.size());
        assertEquals("test2", result.get(0).getName());
        assertEquals(ProjectStatus.IN_PROGRESS, result.get(0).getStatus());

        List<Project> completedProjects = Collections.singletonList(testProjects.get(2));
        when(projectService.findFilteredProjects(ProjectStatus.COMPLETED, null)).thenReturn(completedProjects);
        result = projectController.getFilteredProjects(ProjectStatus.COMPLETED, null);
        assertEquals(1, result.size());
        assertEquals("test3", result.get(0).getName());
        assertEquals(ProjectStatus.COMPLETED, result.get(0).getStatus());
    }

    @Test
    void testFilterProjectsByUser() {
        List<Project> user1Projects = Arrays.asList(testProjects.get(0), testProjects.get(2));
        when(projectService.findFilteredProjects(null, Collections.singletonList(1L))).thenReturn(user1Projects);
        List<Project> result = projectController.getFilteredProjects(null, Collections.singletonList(1L));
        assertEquals(2, result.size());
        assertEquals("test1", result.get(0).getName());
        assertEquals("test3", result.get(1).getName());

        List<Project> user2Projects = Arrays.asList(testProjects.get(1), testProjects.get(3));
        when(projectService.findFilteredProjects(null, Collections.singletonList(2L))).thenReturn(user2Projects);
        result = projectController.getFilteredProjects(null, Collections.singletonList(2L));
        assertEquals(2, result.size());
        assertEquals("test2", result.get(0).getName());
        assertEquals("test4", result.get(1).getName());


        List<Project> user3Projects = Arrays.asList(testProjects.get(2), testProjects.get(3));
        when(projectService.findFilteredProjects(null, Collections.singletonList(3L))).thenReturn(user3Projects);
        result = projectController.getFilteredProjects(null, Collections.singletonList(3L));
        assertEquals(2, result.size());
        assertEquals("test3", result.get(0).getName());
        assertEquals("test4", result.get(1).getName());


        List<Project> multipleUserProjects = Arrays.asList(testProjects.get(0), testProjects.get(1), testProjects.get(2), testProjects.get(3));
        when(projectService.findFilteredProjects(null, Arrays.asList(1L, 2L))).thenReturn(multipleUserProjects);
        result = projectController.getFilteredProjects(null, Arrays.asList(1L, 2L));
        assertEquals(4, result.size());
    }

    @Test
    void testFilterProjectsByStatusAndUser() {
        List<Project> completedUser1Projects = Collections.singletonList(testProjects.get(2));
        when(projectService.findFilteredProjects(ProjectStatus.COMPLETED, Collections.singletonList(1L))).thenReturn(completedUser1Projects);
        List<Project> result = projectController.getFilteredProjects(ProjectStatus.COMPLETED, Collections.singletonList(1L));
        assertEquals(1, result.size());
        assertEquals("test3", result.get(0).getName());
        assertEquals(ProjectStatus.COMPLETED, result.get(0).getStatus());
        assertTrue(result.get(0).getUsers().contains(user1));


        List<Project> onHoldUser3Projects = Collections.singletonList(testProjects.get(3));
        when(projectService.findFilteredProjects(ProjectStatus.ON_HOLD, Collections.singletonList(3L))).thenReturn(onHoldUser3Projects);
        result = projectController.getFilteredProjects(ProjectStatus.ON_HOLD, Collections.singletonList(3L));
        assertEquals(1, result.size());
        assertEquals("test4", result.get(0).getName());
        assertEquals(ProjectStatus.ON_HOLD, result.get(0).getStatus());
        assertTrue(result.get(0).getUsers().contains(user3));
    }

    @Test
    void testNoFiltersReturnsAllProjects() {
        when(projectService.findFilteredProjects(null, null)).thenReturn(testProjects);
        List<Project> result = projectController.getFilteredProjects(null, null);
        assertEquals(5, result.size());
    }

    @Test
    void testService() {
        ProjectService realProjectService = new ProjectService(projectRepository, userRepository, requestRepository, userService);
        when(projectRepository.findAll()).thenReturn(testProjects);
        List<Project> planningProjects = realProjectService.findFilteredProjects(ProjectStatus.PLANNING, null);
        assertEquals(1, planningProjects.size());
        assertEquals("test1", planningProjects.get(0).getName());
        List<Project> user1Projects = realProjectService.findFilteredProjects(null, Collections.singletonList(1L));

        assertEquals(2, user1Projects.size());
        List<Project> completedUser1Projects = realProjectService.findFilteredProjects(ProjectStatus.COMPLETED, Collections.singletonList(1L));
        assertEquals(1, completedUser1Projects.size());
        assertEquals("test3", completedUser1Projects.get(0).getName());

        List<Project> allProjects = realProjectService.findFilteredProjects(null, null);
        assertEquals(5, allProjects.size());
    }
}
