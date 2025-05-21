package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.Project;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectJoinRequestRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProjectRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ProjectServicePrivacyTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectJoinRequestRepository requestRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    private User adminUser;
    private User iMagineerUser;
    private User regularUser;
    private User publicUser;
    private User imagineersOnlyUser;
    private User privateUser;
    private Project testProject;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        // Create admin user with project_write privilege
        adminUser = new User();
        var userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(adminUser, 1L);

        // Add project_write privilege to admin user
        Set<Group> adminGroups = new HashSet<>();
        Group adminGroup = new Group("Admin");
        Set<Privilege> adminPrivileges = new HashSet<>();
        Privilege projectWritePrivilege = new Privilege("project_write");
        adminPrivileges.add(projectWritePrivilege);
        adminGroup.setPrivileges(adminPrivileges);
        adminGroups.add(adminGroup);
        adminUser.setGroups(adminGroups);

        // Create iMagineer user
        iMagineerUser = new User();
        userIdField.set(iMagineerUser, 2L);
        Set<Group> iMagineerGroups = new HashSet<>();
        Group iMagineerGroup = new Group("iMagineer");
        iMagineerGroup.setPrivileges(new HashSet<>());
        iMagineerGroups.add(iMagineerGroup);
        iMagineerUser.setGroups(iMagineerGroups);

        // Create regular user
        regularUser = new User();
        userIdField.set(regularUser, 3L);
        regularUser.setGroups(new HashSet<>());

        // Create users with different privacy levels
        publicUser = new User();
        userIdField.set(publicUser, 4L);
        publicUser.setPrivacyLevel(PrivacyLevel.PUBLIC);

        imagineersOnlyUser = new User();
        userIdField.set(imagineersOnlyUser, 5L);
        imagineersOnlyUser.setPrivacyLevel(PrivacyLevel.IMAGINEERS_ONLY);

        privateUser = new User();
        userIdField.set(privateUser, 6L);
        privateUser.setPrivacyLevel(PrivacyLevel.PRIVATE);

        // Setup test project with members
        testProject = new Project();
        var projectIdField = Project.class.getDeclaredField("id");
        projectIdField.setAccessible(true);
        projectIdField.set(testProject, 1L);

        Set<User> projectMembers = new HashSet<>();
        projectMembers.add(publicUser);
        projectMembers.add(imagineersOnlyUser);
        projectMembers.add(privateUser);
        testProject.setUsers(projectMembers);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void testGetVisibleProjectMembers_AsAdminUser() {
        // Set up security context with admin user
        TestingAuthenticationToken auth = new TestingAuthenticationToken("1", "password", "project_write");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("1")).thenReturn(adminUser);

        // Execute test
        Set<User> visibleMembers = projectService.getVisibleProjectMembers(testProject,adminUser);

        // Admin should see all users
        assertEquals(3, visibleMembers.size());
        assertTrue(visibleMembers.contains(publicUser));
        assertTrue(visibleMembers.contains(imagineersOnlyUser));
        assertTrue(visibleMembers.contains(privateUser));
    }

    @Test
    void testGetVisibleProjectMembers_AsIMagineerUser() {
        // Set up security context with iMagineer user
        TestingAuthenticationToken auth = new TestingAuthenticationToken("2", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("2")).thenReturn(iMagineerUser);

        // Execute test
        Set<User> visibleMembers = projectService.getVisibleProjectMembers(testProject,iMagineerUser);

        // iMagineer should see public and iMagineers_only users
        assertEquals(2, visibleMembers.size());
        assertTrue(visibleMembers.contains(publicUser));
        assertTrue(visibleMembers.contains(imagineersOnlyUser));
    }

    @Test
    void testGetVisibleProjectMembers_AsRegularUser() {
        // Set up security context with regular user
        TestingAuthenticationToken auth = new TestingAuthenticationToken("3", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("3")).thenReturn(regularUser);

        // Execute test
        Set<User> visibleMembers = projectService.getVisibleProjectMembers(testProject,regularUser);

        // Regular user should see only public users
        assertEquals(1, visibleMembers.size());
        assertTrue(visibleMembers.contains(publicUser));
    }
}