package be.uantwerpen.fti.se.imagineframe_backend.model;

import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationResponse {
    private String id;
    private Boolean accepted;
    private List<GroupGetDto> groups;
}
