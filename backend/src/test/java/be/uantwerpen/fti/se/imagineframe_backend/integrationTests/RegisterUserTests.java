package be.uantwerpen.fti.se.imagineframe_backend.integrationTests;

import be.uantwerpen.fti.se.imagineframe_backend.controller.RegistrationController;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.RegistrationGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.RegistrationRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class RegisterUserTests {
    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RegistrationController registrationController;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registrationController).setControllerAdvice(GlobalExceptionHandler.class).build();
        registrationRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_AdminAccept_success() throws Exception {
        // Create registration request
        Registration request = new Registration(
                "Frank.Dew@mail.com",           // email
                "Frank.Dew",                    // username
                "Moon",                         // password
                "Moon",                         // repeatPassword
                "Frank",                        // firstName
                "Dewinne"
        );


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process").contentType(MediaType.APPLICATION_JSON).content(processRequest))
                .andExpect(status().isOk());

        // Check that user is added
        Optional<User> user = userRepository.findByUsername("Frank.Dew");
        assertTrue(user.isPresent());

        // Delete registration request
        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/register/%d", registrationId)))
                .andExpect(status().isOk());

        // Check that registration request is deleted
        Optional<Registration> emptyRequest = registrationRepository.findById(registrationId);
        assertTrue(emptyRequest.isEmpty());
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_AdminDecline_success() throws Exception {
        // Create registration request
        // Create registration request
        Registration request = new Registration(
                "Frank.Dew@mail.com",           // email
                "Frank.Dew",                    // username
                "Moon",                         // password
                "Moon",                         // repeatPassword
                "Frank",                        // firstName
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": false,
                    "groups": []
                }
                """, registrationId);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process").contentType(MediaType.APPLICATION_JSON).content(processRequest))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // Check that user is added
        Optional<User> user = userRepository.findByUsername("Frank.Dew");
        assertTrue(user.isEmpty());

        // Check that registration request is handled
        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/register/%d", registrationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(false));

        // Delete registration request
        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/register/%d", registrationId)))
                .andExpect(status().isOk());

        // Check that registration request is deleted
        Optional<Registration> emptyRequest = registrationRepository.findById(registrationId);
        assertTrue(emptyRequest.isEmpty());
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void acceptUser_NoGroupsGiven_ShouldThrowIllegalArgument() throws Exception {
        // Create registration request
        Registration request = new Registration(
                "Frank.Dew@mail.com",           // email
                "Frank.Dew",                    // username
                "Moon",                         // password
                "Moon",                         // repeatPassword
                "Frank",                        // firstName
                "Dewinne"
        );


        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": []
                }
                """, registrationId);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process").contentType(MediaType.APPLICATION_JSON).content(processRequest))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Assign at least 1 group to which the new user will belong"));

    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_NonExistentRequest_ShouldThrowNotFoundException() throws Exception {
        // Attempt to process a registration request that doesn't exist
        String processRequest = """
                {
                    "id": 999,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("Registration request not found: 999"));

        // Ensure no user was created
        Optional<User> user = userRepository.findByUsername("Frank.Dew");
        assertTrue(user.isEmpty());

        // Also verify the repository still contains nothing under that ID
        Optional<Registration> missingRequest = registrationRepository.findById(999L);
        assertTrue(missingRequest.isEmpty());
    }

    @Test
    @Transactional
    public void registerUser_PasswordsDoNotMatch_ShouldReturnBadRequest() throws Exception {
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "WrongMoon",
                "Frank",
                "Dewinne"
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Passwords do not match"));

        assertTrue(userRepository.findByUsername("Frank.Dew").isEmpty());
        assertEquals(0, registrationRepository.count());
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_DuplicateEmailAtRegistration_ShouldThrowIdentifierException() throws Exception {
        // First registration request (accepted successfully)
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frank",
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isOk());

        // Second registration with same email
        Registration duplicateEmailRequest = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew.2",
                "Moon",
                "Moon",
                "Frankie",
                "Dew"
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Email already in use"));

    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_DuplicateEmailAtAcceptance_ShouldThrowIdentifierException() throws Exception {
        // First registration request (accepted successfully)
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frank",
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId1 = response.getId();
        assertNotNull(registrationId1);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId1);
        assertTrue(presentRequest.isPresent());

        // Second registration with same email
        Registration duplicateEmailRequest = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew.2",
                "Moon",
                "Moon",
                "Frankie",
                "Dew"
        );

        result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId2 = response.getId();
        assertNotNull(registrationId2);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        presentRequest = registrationRepository.findById(registrationId2);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId1);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isOk());

        // Accept registration request
        String processDuplicate = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId2);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processDuplicate))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Email already in use"));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_DuplicateUsernameAtRegistration_ShouldThrowIdentifierException() throws Exception {
        // First registration request (accepted successfully)
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frank",
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isOk());

        // Second registration with same username
        Registration duplicateEmailRequest = new Registration(
                "Frank.Dew@mail2.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frankie",
                "Dew"
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Username already in use"));

    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_DuplicateUsernameAtAcceptance_ShouldThrowIdentifierException() throws Exception {
        // First registration request (accepted successfully)
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frank",
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId1 = response.getId();
        assertNotNull(registrationId1);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId1);
        assertTrue(presentRequest.isPresent());

        // Second registration with same email
        Registration duplicateEmailRequest = new Registration(
                "Frank.Dew@mail2.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frankie",
                "Dew"
        );

        result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmailRequest)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn();

        response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId2 = response.getId();
        assertNotNull(registrationId2);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        presentRequest = registrationRepository.findById(registrationId2);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId1);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isOk());

        // Accept registration request
        String processDuplicate = String.format("""
                {
                    "id": %d,
                    "accepted": true,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId2);

        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processDuplicate))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Username already in use"));
    }

    @Test
    @WithMockUser(username = "admin.ua", authorities = "registration_edit")
    @Transactional
    public void registerUser_AlreadyProcessed_ShouldReturnBadRequest() throws Exception {
        Registration request = new Registration(
                "Frank.Dew@mail.com",
                "Frank.Dew",
                "Moon",
                "Moon",
                "Frank",
                "Dewinne"
        );

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/register/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        RegistrationGetDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                RegistrationGetDto.class
        );

        Long registrationId = response.getId();
        assertNotNull(registrationId);
        assertNull(response.getAccepted());

        // Check that the registration is in the repository
        Optional<Registration> presentRequest = registrationRepository.findById(registrationId);
        assertTrue(presentRequest.isPresent());

        // Accept registration request
        String processRequest = String.format("""
                {
                    "id": %d,
                    "accepted": false,
                    "groups": [{"id": "1"}, {"id": "2"}]
                }
                """, registrationId);
        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isOk());

        // Try processing again
        mockMvc.perform(MockMvcRequestBuilders.post("/register/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processRequest))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Request has already been processed"));
    }
}
