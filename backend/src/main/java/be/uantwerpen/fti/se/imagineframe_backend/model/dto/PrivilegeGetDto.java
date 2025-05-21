package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeGetDto {
    private long id;
    private String name;
    private String description;
}
