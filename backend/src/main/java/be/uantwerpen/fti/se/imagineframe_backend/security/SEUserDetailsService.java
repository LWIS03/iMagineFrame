package be.uantwerpen.fti.se.imagineframe_backend.security;

import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SEUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public SEUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user (@see userdetail.User) based on the username (which in our case is the email). This function is called from AuthController.authenticate().
     * This object has the same name as the normal user, but contains the different privileges which can be interpreted by Spring boot for the different API requests.
     *
     * @param identifier the unique identifier used to search the user (@see User)
     * @return returns a userdetails.User object.
     * @throws UsernameNotFoundException when the user cannot be found in the repository.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        try {
            User user = userRepository.findById(Long.parseLong(identifier)).orElseThrow(() -> new UsernameNotFoundException("User with id " + identifier + " not found"));

            // Get list of user privileges
            Set<Privilege> privileges = user.getGroups().stream()
                    .flatMap(group -> group.getPrivileges().stream()).collect(Collectors.toSet());

            Set<SimpleGrantedAuthority> grantedAuthorities = privileges.stream()
                    .map(privilege -> new SimpleGrantedAuthority(privilege.getName())).collect(Collectors.toSet());

            // Create the user details based on the found user and return it
            return new org.springframework.security.core.userdetails.User(
                    String.valueOf(user.getId()),
                    user.getPassword(),
                    grantedAuthorities);

        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("User with id " + identifier + " not found");
        }
    }
}
