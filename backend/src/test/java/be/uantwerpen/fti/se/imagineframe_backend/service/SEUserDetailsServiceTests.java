package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.security.SEUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class SEUserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @Test
    void loadUsername_Success() {
        // Define behavior
        Long testId = 1L;
        String testPassword = "testPassword";

        Set<Group> groups = new HashSet<>();
        Group group = mock(Group.class);
        groups.add(group);

        Set<Privilege> privileges = new HashSet<>();
        Privilege p1 = new Privilege("Test1");
        Privilege p2 = new Privilege("Test2");
        Privilege p3 = new Privilege("Test3");

        privileges.add(p2);
        privileges.add(p3);
        privileges.add(p1);

        when(group.getPrivileges()).thenReturn(privileges);

        User user = mock(User.class);

        when(user.getId()).thenReturn(testId);
        when(user.getPassword()).thenReturn(testPassword);
        when(user.getGroups()).thenReturn(groups);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Create object
        UserDetailsService userDetailsService = new SEUserDetailsService(userRepository);

        // Test
        UserDetails retrievedUser = userDetailsService.loadUserByUsername("1");

        assert retrievedUser != null;
        assert retrievedUser.getUsername().equals(String.valueOf(testId));
        assert retrievedUser.getPassword().equals(testPassword);

        Collection<? extends GrantedAuthority> authorities = retrievedUser.getAuthorities();
        for (Privilege privilege : privileges) {
            assert authorities.contains(new SimpleGrantedAuthority(privilege.getName()));
        }
    }

    @Test
    void loadUsername_ThrowsUsernameNotFoundException() {
        // Define behavior
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Create object
        UserDetailsService userDetailsService = new SEUserDetailsService(userRepository);

        // Test
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("1"));
    }

    @Test
    void loadUsername_IdNotValid_ThrowsUsernameNotFoundException() {
        // Create object
        UserDetailsService userDetailsService = new SEUserDetailsService(userRepository);

        // Test
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("a"));
    }
}
