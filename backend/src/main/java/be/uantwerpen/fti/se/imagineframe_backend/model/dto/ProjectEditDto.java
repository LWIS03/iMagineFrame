package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ProjectEditDto {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "description is required")
    private String description;

    private String mediaUrl;

    @NotNull(message = "status is required")
    private ProjectStatus status;

    private Set<UserGetDto> users;

    private boolean isPublic = true;
}
