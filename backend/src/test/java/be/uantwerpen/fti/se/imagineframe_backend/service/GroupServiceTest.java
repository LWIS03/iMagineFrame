package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.PrivilegeGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.GroupRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.PrivilegeRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class GroupServiceTest {

    @Value("${administrator_group_name}")
    private String adminGroupName;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @InjectMocks
    private ModelMapper modelMapper;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @BeforeEach
    public void setUp() {
        groupService = new GroupService(groupRepository, userRepository, privilegeRepository);
    }

    @Test
    public void testUpdateGroupInformation_NameOnly() {
        // Arrange
        Group oldGroup = new Group();
        oldGroup.setName("Old Name");
        oldGroup.setUsers(new HashSet<>());
        oldGroup.setPrivileges(new HashSet<>());
        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setName("New Name");
        newGroup.setUsers(new HashSet<>());
        newGroup.setPrivileges(new HashSet<>());

        // Act
        Group updatedGroup = groupService.updateGroupInformation(oldGroup, newGroup);

        // Assert
        assertEquals("New Name", updatedGroup.getName());
    }

    @Test
    public void testUpdateGroupInformation_NullName() {
        // Arrange
        Group oldGroup = new Group();
        oldGroup.setName(null);
        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setName(null);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.updateGroupInformation(oldGroup, newGroup));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Group name is required", exception.getReason());
    }

    @Test
    public void testUpdateGroupInformation_OnlySpacesName() {
        // Arrange
        Group oldGroup = new Group();
        oldGroup.setName("Not null name so test passes");
        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setName("    ");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.updateGroupInformation(oldGroup, newGroup));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Group cannot be empty or all spaces", exception.getReason());
    }

    @Test
    public void testUpdateGroupInformation_EmptyName() {
        // Arrange
        Group oldGroup = new Group();
        oldGroup.setName("Not null name so test passes");
        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setName("");

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.updateGroupInformation(oldGroup, newGroup));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Group cannot be empty or all spaces", exception.getReason());
    }

    @Test
    public void testUpdateAdminGroup_NameOnly() {
        // Arrange
        Group adminGroup = mock(Group.class);
        when(adminGroup.getName()).thenReturn(adminGroupName);
        when(adminGroup.getId()).thenReturn(1L);

        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setName("OtherName");


        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.updateGroupInformation(adminGroup, newGroup));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, exception.getStatusCode());
        assertEquals("Administrator group name cannot be changed", exception.getReason());
    }

    @Test
    public void testUpdateGroupInformation_UsersOnly() {
        // Arrange
        Group oldGroup = new Group();
        oldGroup.setName("Test Group");
        oldGroup.setUsers(new HashSet<>());
        oldGroup.setPrivileges(new HashSet<>());


        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setPrivileges(new HashSet<>());

        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Set<UserGetDto> userGetDtos = new HashSet<>();
        userGetDtos.add(modelMapper.map(user, UserGetDto.class));

        newGroup.setUsers(userGetDtos);

        // Act
        Group updatedGroup = groupService.updateGroupInformation(oldGroup, newGroup);

        // Assert
        assertNotNull(updatedGroup.getUsers());
        assertEquals(1, updatedGroup.getUsers().size());
    }

    @Test
    public void testUpdateGroupInformation_AdminGroup_RemoveAllUsers() {
        // Arrange
        Group adminGroup = mock(Group.class);
        when(adminGroup.getId()).thenReturn(1L);
        when(adminGroup.getName()).thenReturn("something");
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(adminGroup.getUsers()).thenReturn(new HashSet<>(Set.of(user)));

        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setUsers(new HashSet<>());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.updateGroupInformation(adminGroup, newGroup));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, exception.getStatusCode());
        assertEquals("Cannot remove all users from admin group", exception.getReason());
    }

    @Test
    public void testDeleteByID() {
        // Arrange
        long groupID = 1L;
        Group group = new Group();
        group.setName("Test Group");
        Set<User> users = new HashSet<>();
        User user = new User();
        users.add(user);
        group.setUsers(users);
        user.setGroups(new HashSet<>(Set.of(group)));

        when(groupRepository.findById(groupID)).thenReturn(Optional.of(group));

        // Act
        groupService.deleteByID(groupID);

        // Assert
        verify(groupRepository, times(1)).deleteById(groupID);
        assertTrue(user.getGroups().isEmpty());
    }

    @Test
    public void testUpdateGroupInformation_PrivilegesOnly() {
        // Change name
            // NVT
        // Create privileges
        PrivilegeGetDto privilege = new PrivilegeGetDto();
        Set<PrivilegeGetDto> newPrivileges = new HashSet<>(Set.of(privilege));

        // Create users
        Set<UserGetDto> users = new HashSet<>();


        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setPrivileges(newPrivileges);
        newGroup.setUsers(users);

        Group oldGroup = new Group();
        oldGroup.setName("Test Group");
        oldGroup.setPrivileges(new HashSet<>());


        when(privilegeRepository.findById(0L)).thenReturn(Optional.of(new Privilege()));

        // Act
        Group updatedGroup = groupService.updateGroupInformation(oldGroup, newGroup);

        // Assert
        assertEquals(1, updatedGroup.getPrivileges().size());
    }

    @Test
    public void testUpdateGroupInformation_PrivilegesOnly_PrivilegeNotFound() {
        // Change name
        // NVT
        // Create privileges
        PrivilegeGetDto privilege = new PrivilegeGetDto();
        Set<PrivilegeGetDto> newPrivileges = new HashSet<>(Set.of(privilege));

        // Create users
        Set<UserGetDto> users = new HashSet<>();


        GroupEditDto newGroup = new GroupEditDto();
        newGroup.setPrivileges(newPrivileges);
        newGroup.setUsers(users);

        Group oldGroup = new Group();
        oldGroup.setName("Test Group");
        oldGroup.setPrivileges(new HashSet<>());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> groupService.updateGroupInformation(oldGroup, newGroup));
        assertEquals("0", exception.getEntityId());
        assertEquals("Privilege", exception.getEntityName());
    }

    @Test
    public void testFindGroup_Success() {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(1L);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        Group result = groupService.findGroup(1L);

        assertEquals(group, result);
    }

    @Test
    public void testFindGroup_Failure() {
        when(groupRepository.findById(1L)).thenThrow(new RuntimeException());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> groupService.findGroup(1L));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Group could not be found", exception.getReason());
    }

    @Test
    public void testDeleteUserFromGroup() {
        Group group = new Group();
        User user = new User();

        group.setUsers(new HashSet<>(Set.of(user)));
        user.setGroups(new HashSet<>(Set.of(group)));

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        groupService.deleteUserFromGroup(1L, user);

        assertFalse(group.getUsers().contains(user));
        assertFalse(user.getGroups().contains(group));
    }
}
