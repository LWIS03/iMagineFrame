package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.repository.PrivilegeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PrivilegeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PrivilegeRepository privilegeRepository;

    @InjectMocks
    private PrivilegeController privilegeController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(privilegeController).build();
    }

    @Test
    public void testGetPrivileges() throws Exception {
        // Arrange
        Privilege privilege1 = new Privilege("Privilege1", "Description1");
        Privilege privilege2 = new Privilege("Privilege2", "Description2");
        List<Privilege> privileges = Arrays.asList(privilege1, privilege2);

        when(privilegeRepository.findAll()).thenReturn(privileges);

        // Act & Assert
        mockMvc.perform(get("/privileges")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Privilege1"))
                .andExpect(jsonPath("$[1].name").value("Privilege2"));
    }
}
