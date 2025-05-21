package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.IdentifierException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.PasswordException;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ChangePasswordDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTests {

    @Mock
    private UserEditDto newUserDto;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");

        userService = new UserService(userRepository, groupService, passwordEncoder);
    }

    @Test
    void generateUsernames_failed(){
        String firstName = "firstName";
        String lastName = null;

        assertThrows(Exception.class, () -> userService.generateUsername(firstName, lastName));
    }

    @Test
    void createUserWithGeneratedUsername_success() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getPassword()).thenReturn("password");
        when(newUserDto.getRepeatPassword()).thenReturn("password");
        when(newUserDto.getGroups()).thenReturn(null);


        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        User oldUserResult = userService.updateUserInformation(oldUser, newUserDto);

        // Assert the information is correctly transferred
        assert oldUserResult != null;
        assert oldUserResult.getFirstName().equals("John");
        assert oldUserResult.getLastName().equals("Doe");
        assert oldUserResult.getEmail().equals("john.doe@mail.com");
        assert oldUserResult.getUsername().startsWith("Doe.J");
    }

    @Test
    void createUserWithUsername_Success() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getUsername()).thenReturn("JohnTheDoe");
        when(newUserDto.getGroups()).thenReturn(null);
        when(newUserDto.getPassword()).thenReturn("password");
        when(newUserDto.getRepeatPassword()).thenReturn("password");


        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        User oldUserResult = userService.updateUserInformation(oldUser, newUserDto);

        // Assert the information is correctly transferred
        assert oldUserResult != null;
        assert oldUserResult.getFirstName().equals("John");
        assert oldUserResult.getLastName().equals("Doe");
        assert oldUserResult.getEmail().equals("john.doe@mail.com");
        assert oldUserResult.getUsername().equals("JohnTheDoe");
    }

    @Test
    void createUserWithoutUsername_ThrowsException() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getGroups()).thenReturn(null);

        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        assertThrows(IdentifierException.class, () -> userService.updateUserInformation(oldUser, newUserDto));
    }

    @Test
    void createUserWithoutEmail_ThrowsException() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getGroups()).thenReturn(null);

        UserService userService = new UserService(userRepository, groupService, passwordEncoder);

        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        assertThrows(IdentifierException.class, () -> userService.updateUserInformation(oldUser, newUserDto));
    }

    @Test
    void createUserWithDuplicateEmailTest_ThrowsException() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getGroups()).thenReturn(null);

        // Define reaction of userRepository
        when(userRepository.findByEmail(newUserDto.getEmail())).thenReturn(Optional.of(new User()));

        UserService userService1 = new UserService(userRepository, groupService, passwordEncoder);

        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        assertThrows(IdentifierException.class, () -> userService1.updateUserInformation(oldUser, newUserDto));
    }

    @Test
    void createUserWithoutPassword_ThrowsException() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getGroups()).thenReturn(null);

        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        assertThrows(PasswordException.class, () -> {
            userService.updateUserInformation(oldUser, newUserDto);
        });
    }

    @Test
    void createUserWithEmptyPassword_ThrowsException() {
        // Set behavior of all mocks
        // Define reaction of newUser
        when(newUserDto.getFirstName()).thenReturn("John");
        when(newUserDto.getLastName()).thenReturn("Doe");
        when(newUserDto.getEmail()).thenReturn("john.doe@mail.com");
        when(newUserDto.getPassword()).thenReturn("  ");
        when(newUserDto.getGroups()).thenReturn(null);

        // This mimics the behavior when creating a new user at the API endpoint
        User oldUser = new User();
        assertThrows(PasswordException.class, () -> {
            userService.updateUserInformation(oldUser, newUserDto);
        });
    }

    @Test
    void findUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findUser("1");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findUser("test@example.com");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void findUserByUsername_Success() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findUser("testUser");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void findUser_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findUser("unknownUser"));
    }

    @Test
    void findUser_InvalidIdFormat_ShouldSearchByUsernameOrEmail() {
        when(userRepository.findByUsername("notAnId")).thenReturn(Optional.of(testUser));

        User foundUser = userService.findUser("notAnId");

        assertNotNull(foundUser);
        assertEquals("testUser", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("notAnId");
    }

    @Test
    void updateUserInformation_MissingEmailForNewUser_ShouldThrowEmailIsRequired() {
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail(null);

        // Act + Assert
        IdentifierException exception = assertThrows(IdentifierException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Email is required", exception.getMessage());
    }

    @Test
    void updateUserInformation_NewEmailAlreadyExistsInDatabase_ShouldThrowEmailAlreadyInUse() {
        // Arrange
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail("new@example.com");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(new User()));

        // Act + Assert
        IdentifierException exception = assertThrows(IdentifierException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Email already in use", exception.getMessage());
    }

    @Test
    void updateUserInformation_MissingUsernameForNewUser_ShouldThrowUsernameIsRequired() {
        // Arrange
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail("email@example.com");
        newUser.setUsername(null);
        newUser.setFirstName(null);

        // Act + Assert
        IdentifierException exception = assertThrows(IdentifierException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Username is required", exception.getMessage());
    }

    @Test
    void updateUserInformation_NewUsernameAlreadyExistsInDatabase_ShouldThrowUsernameAlreadyInUse() {
        // Arrange
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail("email@example.com");
        newUser.setUsername("newuser");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(new User()));

        // Act + Assert
        IdentifierException exception = assertThrows(IdentifierException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Username already in use", exception.getMessage());
    }

    @Test
    void updateUserInformation_PasswordsNotProvided_ShouldThrowPasswordException() {
        // Arrange
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail("email@example.com");
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");

        // Act + Assert
        PasswordException exception = assertThrows(PasswordException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Passwords should match", exception.getMessage());
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordDoesNotMatch() {
        // Arrange
        User user = new User();
        user.setPassword("hashedPassword");

        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword("newPassword");
        dto.setNewPasswordRepeated("newPassword");

        when(passwordEncoder.matches("hashedPassword", "wrongPassword")).thenReturn(false);

        // Act & Assert
        PasswordException exception = assertThrows(PasswordException.class, () -> {
            userService.changePassword(user, dto);
        });

        assertEquals("Old password does not match", exception.getMessage());
    }

    @Test
    void changePassword_success() {
        // Arrange
        User user = new User();
        user.setPassword("hashedPassword");

        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("correctPassword");
        dto.setNewPassword("newSecurePassword");
        dto.setNewPasswordRepeated("newSecurePassword");

        when(passwordEncoder.matches("correctPassword", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword")).thenReturn("encodedNewPassword");

        UserService userService = new UserService(userRepository, groupService, passwordEncoder);

        // Act
        userService.changePassword(user, dto);

        // Assert
        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }


    @Test
    void updateUserInformation_PasswordsDoNoMatch_ShouldThrowPasswordException() {
        // Arrange
        UserEditDto newUser = new UserEditDto();
        newUser.setEmail("email@example.com");
        newUser.setUsername("newuser");
        newUser.setPassword("newpassword");
        newUser.setRepeatPassword("otherpassword");

        // Act + Assert
        PasswordException exception = assertThrows(PasswordException.class, () ->
                userService.updateUserInformation(new User(), newUser)
        );
        assertEquals("Passwords should match", exception.getMessage());
    }
}

