package be.uantwerpen.fti.se.imagineframe_backend.security;

import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JWTUtilTests {

    @Value("${jwt_secret}")
    private String secret;

    @Mock
    private User user;

    @Mock
    private Group adminGroup;

    private final Set<Privilege> privileges = new HashSet<>();

    @Autowired
    private JWTUtil jwtUtil;

    @BeforeEach
    public void setUserMockBehaviour() {
        when(user.getEmail()).thenReturn("John.Doe@email.com");
        when(user.getFirstName()).thenReturn("John");
        when(user.getLastName()).thenReturn("Doe");
        when(user.getId()).thenReturn(1L);
        when(user.getGroups()).thenReturn(Set.of(adminGroup));
    }

    @BeforeAll
    public void setGroupMockBehaviour() {
        when(adminGroup.getName()).thenReturn("Admin");
        Privilege p1 = new Privilege("Test1");
        Privilege p2 = new Privilege("Test2");
        Privilege p3 = new Privilege("Test3");
        privileges.add(p1);
        privileges.add(p2);
        privileges.add(p3);
        when(adminGroup.getPrivileges()).thenReturn(privileges);
    }

    public DecodedJWT decodeJWTToken(String token) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
                .withIssuer("UA_FTI_SE_IMAGINEFRAME")
                .build();

        return verifier.verify(token);
    }

    @Test
    public void createJWTToken() {
        String createdToken = jwtUtil.generateToken(user);

        DecodedJWT decodedJWT = decodeJWTToken(createdToken);

        assertNotNull(decodedJWT);
        assertEquals("User Details", decodedJWT.getSubject());
        assertEquals("1", String.valueOf(decodedJWT.getClaim("id")));
        List<Privilege> decodedPrivileges = decodedJWT.getClaim("privileges").asList(Privilege.class);

        // Sort lists in order for the values to match
        List<Privilege> privilegesList = new ArrayList<>(privileges.stream().toList());
        privilegesList.sort(Comparator.comparing(Privilege::getName));
        decodedPrivileges.sort(Comparator.comparing(Privilege::getName));

        for (int i = 0; i < privilegesList.size(); i++) {
            assert decodedPrivileges.get(i).getName().equals(privilegesList.get(i).getName());
        }
    }

    @Test
    public void validateJWTToken() {
        String createdToken = jwtUtil.generateToken(user);

        String id = jwtUtil.validateTokenAndRetrieveSubject(createdToken);

        assertNotNull(id);
        assertEquals("1", id);
    }
}
