package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProjectService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProjectControllerAdditionalTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPublicProjects() {
        Project publicProject1 = new Project();
        publicProject1.setName("Public Project 1");
        publicProject1.setPublic(true);
        Project publicProject2 = new Project();
        publicProject2.setName("Public Project 2");
        publicProject2.setPublic(true);

        List<Project> publicProjects = Arrays.asList(publicProject1, publicProject2);
        when(projectService.findPublicProjects()).thenReturn(publicProjects);
        List<Project> result = projectController.getPublicProjects();

        assertEquals(2, result.size());
        assertTrue(result.contains(publicProject1));
        assertTrue(result.contains(publicProject2));
        verify(projectService, times(1)).findPublicProjects();
    }

    @Test
    void testGetVisibleProjectUsers() throws Exception {
        Project project = new Project();
        project.setName("Test Project");
        java.lang.reflect.Field idField = Project.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(project, 1L);
        User publicUser = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(publicUser, 1L);
        publicUser.setUsername("public_user");
        publicUser.setPrivacyLevel(PrivacyLevel.PUBLIC);
        UserGetDto publicUserDto = new UserGetDto();
        publicUserDto.setId(1L);
        publicUserDto.setUsername("public_user");
        Set<User> visibleUsers = new HashSet<>();
        visibleUsers.add(publicUser);
        String userId = "1";
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userService.findUser(userId)).thenReturn(publicUser);
        SecurityContextHolder.setContext(mock(SecurityContext.class));
        Authentication authentication = mock(Authentication.class);
        when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(userId);
        when(projectService.getVisibleProjectMembers(project, publicUser)).thenReturn(visibleUsers);
        when(modelMapper.map(publicUser, UserGetDto.class)).thenReturn(publicUserDto);
        List<UserGetDto> result = projectController.getVisibleProjectUsers(1L);
        assertEquals(1, result.size());
        assertEquals("public_user", result.get(0).getUsername());
        verify(projectService, times(1)).getVisibleProjectMembers(project, publicUser);
    }
}