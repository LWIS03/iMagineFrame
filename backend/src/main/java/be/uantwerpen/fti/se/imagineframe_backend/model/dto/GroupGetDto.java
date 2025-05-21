package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupGetDto {
    private long id;
    private String name;
    private Set<PrivilegeGetDto> privileges;
    private Set<UserGetDto> users;

    public GroupGetDto(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
