package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserGetDto {
    long id;
    String firstName;
    String lastName;
    String username;
    String email;
    Set<GroupGetDto> groups;
    String privacyLevel;

    //renew token
    // ★Newly added field: used by the backend to return a new Token
    private String jwtToken;

    public UserGetDto(long id, String firstName, String lastName, String username, String email, Set<GroupGetDto> groups) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.groups = groups;
    }

    public UserGetDto(long id, String firstName, String lastName, String username, String email, Set<GroupGetDto> groups, String privacyLevel) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.groups = groups;
        this.privacyLevel = privacyLevel;
    }

}
