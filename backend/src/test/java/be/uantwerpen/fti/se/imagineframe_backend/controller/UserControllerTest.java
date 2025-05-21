package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "admin_read")
    void getUsers_ShouldReturnListOfUsers() throws Exception {
        User user = new User();
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(any(), eq(UserGetDto.class))).thenReturn(new UserGetDto());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").exists());
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "admin_write")
    void getUserById_ShouldReturnUser() throws Exception {
        User user = new User();

        User loggedInUser = mock(User.class);
        when(loggedInUser.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(any(), eq(UserGetDto.class))).thenReturn(new UserGetDto());
        when(userRepository.save(any())).thenReturn(user);
        when(userService.updateUserInformation(any(User.class), any(UserEditDto.class))).thenReturn(user);
        when(userService.findUser("email@test.be")).thenReturn(loggedInUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "admin_write")
    void getUserById_NotFound_ShouldReturn404() throws Exception {
        User loggedInUser = mock(User.class);
        when(loggedInUser.getId()).thenReturn(1L);
        when(userService.findUser("email@test.be")).thenReturn(loggedInUser);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @WithMockUser(username="email@test.be", authorities = "user_edit")
    void createUser_ShouldCreateUser() throws Exception {
        UserEditDto userEditDto = new UserEditDto();
        userEditDto.setEmail("email@test.be");
        when(userService.updateUserInformation(any(), any())).thenReturn(new User());

        mockMvc.perform(put("/users/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEditDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "admin_write")
    void updateUser_EmailMandatory() throws Exception {
        User user = new User();
        UserEditDto userEditDto = new UserEditDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userService.updateUserInformation(any(User.class), any(UserEditDto.class))).thenReturn(user);
        when(userService.findUser(userEditDto.getEmail())).thenReturn(new User());

        mockMvc.perform(post("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEditDto)))
                .andExpect(content().string(containsString("Email is mandatory")));
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "admin_write")
    void updateUser_UpdateUserSuccess() throws Exception {
        User user = new User();
        UserEditDto userEditDto = new UserEditDto();
        userEditDto.setEmail("email@test.be");
        userEditDto.setUsername("username");
        userEditDto.setPassword("password");
        userEditDto.setFirstName("firstName");
        userEditDto.setLastName("lastName");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);
        when(userService.updateUserInformation(any(User.class), any(UserEditDto.class))).thenReturn(user);
        when(userService.findUser(userEditDto.getEmail())).thenReturn(new User());

        mockMvc.perform(post("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEditDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "admin_write")
    void updateUser_NotFound_ShouldReturn404() throws Exception {
        UserEditDto userEditDto = new UserEditDto();
        userEditDto.setEmail("email@test.be");

        User loggedInUser = mock(User.class);
        when(loggedInUser.getId()).thenReturn(1L);
        when(userService.findUser("email@test.be")).thenReturn(loggedInUser);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userEditDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("User not found")));
    }

    @Test
    @WithMockUser(authorities = "user_edit", username = "testuser@example.com")
    void deleteUser_Success() throws Exception {
        User user = new User();
        user.setEmail("testuser@example.com");
        when(userService.findUser("testuser@example.com")).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "user_edit", username = "testuser@example.com")
    void deleteUser_deleteYourself() throws Exception {
        User user = Mockito.mock(User.class);
        when(user.getId()).thenReturn(2L);
        user.setEmail("testuser@example.com");
        user.setUsername("testUser");

        when(userService.findUser("testuser@example.com")).thenReturn(user);

        mockMvc.perform(delete("/users/2"))
                .andExpect(status().isMethodNotAllowed());
    }
}
