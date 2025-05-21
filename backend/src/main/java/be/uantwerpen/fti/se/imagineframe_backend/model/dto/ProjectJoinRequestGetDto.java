package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectJoinRequestGetDto {
    private long id;
    private long userId;
    private String username;
    private long projectId;
    private String projectName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Boolean accepted;
}