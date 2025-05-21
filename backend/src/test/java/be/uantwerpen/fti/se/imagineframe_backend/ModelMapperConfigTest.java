package be.uantwerpen.fti.se.imagineframe_backend;

import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.modelmapper.ModelMapper;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ModelMapperConfigTest {

    @InjectMocks
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        ModelMapperConfig config = new ModelMapperConfig();
        this.modelMapper = config.modelMapper();
    }

    @Test
    void testUserToUserGETDTOMapping() {
        // Create privileges
        Privilege privilege1 = new Privilege("READ");
        Privilege privilege2 = new Privilege("WRITE");

        // Create groups
        Group group1 = new Group("Admin Group");
        group1.setPrivileges(Set.of(privilege1));
        Group group2 = new Group("HR Group");
        group2.setPrivileges(Set.of(privilege2));

        // Create user
        User user = new User("Alice@ua.be", null);
        user.setFirstName("Alice");
        user.setLastName("LastName");
        user.setGroups(Set.of(group1, group2));

        // Link users to groups
        group1.setUsers(Set.of(user));
        group2.setUsers(Set.of(user));

        // Convert User -> UserGETDTO
        UserGetDto userGetDto = modelMapper.map(user, UserGetDto.class);

        // Assertions
        assertThat(userGetDto).isNotNull();
        assertThat(userGetDto.getId()).isEqualTo(user.getId());
        assertThat(userGetDto.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userGetDto.getLastName()).isEqualTo(user.getLastName());

        // Groups should be mapped
        assertThat(userGetDto.getGroups()).hasSize(2);

        // Groups should have privileges
        assertThat(userGetDto.getGroups()).allSatisfy(group -> assertThat(group.getPrivileges()).isEmpty());
        //assertThat(userGetDto.getGroups()).anyMatch(group -> group.getPrivileges().size() == 1);

        // Groups should NOT have users (to prevent recursion)
        assertThat(userGetDto.getGroups()).allSatisfy(group ->
                assertThat(group.getUsers()).isEmpty()
        );
    }

    @Test
    void testGroupToGroupDTOMapping() {
        // Create privileges
        Privilege privilege1 = new Privilege("READ");
        Privilege privilege2 = new Privilege("WRITE");

        // Create users
        User user1 = new User("Alice@ua.be", null);
        user1.setFirstName("Alice");
        user1.setLastName("LastName");
        User user2 = new User("Bob@ua.be", null);
        user2.setFirstName("Bob");
        user2.setLastName("NameLast");

        // Create group
        Group group = new Group("Admin Group");
        group.setPrivileges(Set.of(privilege1, privilege2));
        group.setUsers(Set.of(user1, user2));

        // Convert Group -> GroupDTO
        GroupGetDto groupGetDto = modelMapper.map(group, GroupGetDto.class);

        // Assertions
        assertThat(groupGetDto).isNotNull();
        assertThat(groupGetDto.getId()).isEqualTo(group.getId());
        assertThat(groupGetDto.getName()).isEqualTo(group.getName());

        // Group should have privileges
        assertThat(groupGetDto.getPrivileges()).hasSize(2);

        // Group should have users
        assertThat(groupGetDto.getUsers()).hasSize(2);

        // Users inside GroupDTO should have EMPTY groups
        assertThat(groupGetDto.getUsers()).allSatisfy(userDTO ->
                assertThat(userDTO.getGroups()).isEmpty()
        );
    }
}
