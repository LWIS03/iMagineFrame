package be.uantwerpen.fti.se.imagineframe_backend.integrationTests;

import be.uantwerpen.fti.se.imagineframe_backend.controller.AuthController;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.LoginCredentials;
import be.uantwerpen.fti.se.imagineframe_backend.repository.PrivilegeRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class LoginTests {

    @Autowired
    private MockMvc mvc;

    @Value("${jwt_secret}")
    private String secret;

    @Autowired
    private AuthController authController;

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PrivilegeRepository privilegeRepository;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void loginAdmin_withEmail_success() throws Exception {
        LoginCredentials login = new LoginCredentials("admin@uantwerpen.be", "admin");

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> parsedResult = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String token = parsedResult.get("jwt-token");

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        List<Privilege> decodedPrivileges = jwt.getClaim("privileges").asList(Privilege.class);
        String decodedId = String.valueOf(jwt.getClaim("id"));

        // Sort lists in order for the values to match
        List<Privilege> privileges = new ArrayList<>();
        for (Privilege privilege : privilegeRepository.findAll()) {
            privileges.add(privilege);
        }
        privileges.sort(Comparator.comparing(Privilege::getName));
        decodedPrivileges.sort(Comparator.comparing(Privilege::getName));

        assertEquals("1", decodedId);
        for (int i = 0; i < privileges.size(); i++) {
            assert decodedPrivileges.get(i).getName().equals(privileges.get(i).getName());
        }
    }

    @Test
    public void loginAdmin_withUsername_success() throws Exception {
        LoginCredentials login = new LoginCredentials("admin.ua", "admin");

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> parsedResult = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String token = parsedResult.get("jwt-token");

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        List<Privilege> decodedPrivileges = jwt.getClaim("privileges").asList(Privilege.class);
        String decodedId = String.valueOf(jwt.getClaim("id"));

        // Sort lists in order for the values to match
        List<Privilege> privileges = new ArrayList<>();
        for (Privilege privilege : privilegeRepository.findAll()) {
            privileges.add(privilege);
        }
        privileges.sort(Comparator.comparing(Privilege::getName));
        decodedPrivileges.sort(Comparator.comparing(Privilege::getName));

        assertEquals("1", decodedId);
        for (int i = 0; i < privileges.size(); i++) {
            assert decodedPrivileges.get(i).getName().equals(privileges.get(i).getName());
        }
    }

    @Test
    public void loginAdmin_withWrongPassword_unsuccessful() throws Exception {
        LoginCredentials login = new LoginCredentials("john.doe", "wrongPassword");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void loginTester_withEmail_success() throws Exception {
        String email = "john.doe@uantwerpen.be";
        LoginCredentials login = new LoginCredentials(email, "password");

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> parsedResult = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String token = parsedResult.get("jwt-token");

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        List<Privilege> decodedPrivileges = jwt.getClaim("privileges").asList(Privilege.class);
        String decodedUserId = String.valueOf(jwt.getClaim("id"));

        // Sort lists in order for the values to match
        User user = userService.findUser(email);
        Set<Group> groups = user.getGroups();
        List<Privilege> privileges = new ArrayList<>();
        for (Group group : groups) {
            privileges.addAll(group.getPrivileges());
        }
        privileges.sort(Comparator.comparing(Privilege::getName));
        decodedPrivileges.sort(Comparator.comparing(Privilege::getName));


        assertEquals("2", decodedUserId);
        for (int i = 0; i < privileges.size(); i++) {
            assert decodedPrivileges.get(i).getName().equals(privileges.get(i).getName());
        }
    }

    @Test
    public void loginTester_withUsername_success() throws Exception {
        String userName = "john.doe";
        LoginCredentials login = new LoginCredentials(userName, "password");

        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, String> parsedResult = mapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String token = parsedResult.get("jwt-token");

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();

        DecodedJWT jwt = verifier.verify(token);
        List<Privilege> decodedPrivileges = jwt.getClaim("privileges").asList(Privilege.class);
        String decodedId = String.valueOf(jwt.getClaim("id"));

        // Sort lists in order for the values to match
        User user = userService.findUser(userName);
        Set<Group> groups = user.getGroups();
        List<Privilege> privileges = new ArrayList<>();
        for (Group group : groups) {
            privileges.addAll(group.getPrivileges());
        }
        privileges.sort(Comparator.comparing(Privilege::getName));
        decodedPrivileges.sort(Comparator.comparing(Privilege::getName));

        assertEquals("2", decodedId);
        for (int i = 0; i < privileges.size(); i++) {
            assert decodedPrivileges.get(i).getName().equals(privileges.get(i).getName());
        }
    }

    @Test
    public void login_WithWrongEmail_returnStatus401() throws Exception {
        LoginCredentials login = new LoginCredentials("not.exist@uantwerpen.be", "password");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
