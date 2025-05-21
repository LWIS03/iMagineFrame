package be.uantwerpen.fti.se.imagineframe_backend.integrationTests;


import be.uantwerpen.fti.se.imagineframe_backend.controller.UserController;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ChangePasswordDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.*;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import be.uantwerpen.fti.se.imagineframe_backend.controller.RegistrationController;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.RegistrationGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ChangePasswordTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserController userController;
    @Autowired
    private ProjectRepository projectRepository;

    private long currentUserId;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final Logger logger = Logger.getLogger(ChangePasswordTests.class.getName());


    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(GlobalExceptionHandler.class).build();
        projectRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Add an already existing user
        UserEditDto editDto = new UserEditDto();
        editDto.setFirstName("John");
        editDto.setLastName("Doe");
        editDto.setUsername("John.doe");
        editDto.setPassword("oldPassword");
        editDto.setRepeatPassword("oldPassword");
        editDto.setEmail("john.doe@example.com");

        currentUserId = userRepository.save(userService.updateUserInformation(new User(), editDto)).getId();

        assertEquals(1, userRepository.count());

        editDto.setFirstName("Admin");
        editDto.setLastName("UA");
        editDto.setUsername("admin.ua");
        editDto.setPassword("oldPassword");
        editDto.setRepeatPassword("oldPassword");
        editDto.setEmail("admin@example.com");

        userRepository.save(userService.updateUserInformation(new User(), editDto));
    }

    @Test
    @WithMockUser(username = "John.doe", authorities = {"logon"})
    void changePassword_successful() throws Exception {
        // Create passwordChange object
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("oldPassword");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setNewPasswordRepeated("newPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/changePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isOk());

        // Check that the password is changed
        User user = userRepository.findByUsername("John.doe").get();
        assertTrue(passwordEncoder.matches("newPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "John.doe", authorities = {"logon"})
    void changePassword_passwordsDoNotMatch_ShouldThrowException() throws Exception {
        // Create passwordChange object
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("oldPassword");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setNewPasswordRepeated("repeatedPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/changePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isBadRequest());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertFalse(passwordEncoder.matches("newPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = {"logon"})
    void changePassword_NotUser_ShouldThrowException() throws Exception {
        // Create passwordChange object
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("oldPassword");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setNewPasswordRepeated("newPasswordRepeated");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/changePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isMethodNotAllowed());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertTrue(passwordEncoder.matches("oldPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "John.doe", authorities = {"logon"})
    void changePassword_NotRightOldPassword_ShouldThrowException() throws Exception {
        // Create passwordChange object
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setOldPassword("wrongPassword");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setNewPasswordRepeated("newPassword");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/changePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertTrue(passwordEncoder.matches("oldPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = {"password_edit"})
    void forceChangePassword_successful() throws Exception {
        String newPassword = "newPassword";
        String newPasswordRepeated = "newPassword";

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNewPassword(newPassword);
        changePasswordDto.setNewPasswordRepeated(newPasswordRepeated);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/forceChangePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = {"logon"})
    void forceChangePassword_notRightPrivilege_shouldThrowError() throws Exception {
        String newPassword = "newPassword";
        String newPasswordRepeated = "repeatedPassword";

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNewPassword(newPassword);
        changePasswordDto.setNewPasswordRepeated(newPasswordRepeated);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/forceChangePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                //.andDo(MockMvcResultHandlers.print())
                .andExpect(status().is5xxServerError());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertFalse(passwordEncoder.matches(newPassword, user.getPassword()));
        assertTrue(passwordEncoder.matches("oldPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = {"password_edit"})
    void forceChangePassword_newPasswordIsEmpty_throwsPasswordException() throws Exception {
        String newPassword = "  ";

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNewPassword(newPassword);
        changePasswordDto.setNewPasswordRepeated(newPassword);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/forceChangePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertFalse(passwordEncoder.matches(newPassword, user.getPassword()));
        assertTrue(passwordEncoder.matches("oldPassword", user.getPassword()));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = {"password_edit"})
    void forceChangePassword_repeatedPasswordIsEmpty_throwsPasswordException() throws Exception {
        String newPassword = "Test";
        String repeatedPassword = "   ";

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setNewPassword(newPassword);
        changePasswordDto.setNewPasswordRepeated(repeatedPassword);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/users/{id}/forceChangePassword", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

        // Check that the password is not changed
        User user = userRepository.findByUsername("John.doe").get();
        assertFalse(passwordEncoder.matches(newPassword, user.getPassword()));
        assertTrue(passwordEncoder.matches("oldPassword", user.getPassword()));
    }
}