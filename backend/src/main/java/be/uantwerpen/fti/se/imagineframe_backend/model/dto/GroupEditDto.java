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
public class GroupEditDto {
    private String name;
    private Set<PrivilegeGetDto> privileges;
    private Set<UserGetDto> users;
}
