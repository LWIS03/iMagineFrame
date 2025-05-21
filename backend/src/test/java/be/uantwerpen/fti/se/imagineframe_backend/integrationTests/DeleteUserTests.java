package be.uantwerpen.fti.se.imagineframe_backend.integrationTests;

import be.uantwerpen.fti.se.imagineframe_backend.controller.UserController;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.repository.GroupRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class DeleteUserTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Autowired
    private GroupRepository groupRepository;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(GlobalExceptionHandler.class).build();
    }

    @Test
    @WithMockUser(authorities = "user_edit", username = "admin.ua")
    @Transactional
    void deleteUser_Success() throws Exception {
        // Delete user with id 50 --> from tester group
        mockMvc.perform(delete("/users/50"))
                .andExpect(status().isOk());

        // Check if the user is deleted from the database
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> userService.findUser("50"));
        assertTrue(exception.getMessage().contains("Could not find user with id 50"));

        // Check if the suer is deleted from the Tester group
        Optional<Group> testerGroup = groupRepository.findById(1L);
        assertTrue(testerGroup.isPresent());

        Group group = testerGroup.get();
        assertFalse(group.getUsers().stream().anyMatch(user -> user.getId() == 50));
    }

    @Test
    @WithMockUser(authorities = "user_edit", username = "admin.ua")
    @Transactional
    void deleteUser_deleteYourself() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isMethodNotAllowed());

        // Check that the user is NOT deleted from the database
        assertEquals(1L, userService.findUser("1").getId());
    }
}
