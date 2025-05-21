package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectGetDto {
    private long id;
    private String name;
    private String description;
    private String mediaUrl;
    private ProjectStatus status;
    private boolean isPublic;
    private UserGetDto owner;
}