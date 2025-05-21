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
public class RegistrationGetDto {
    private long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Boolean accepted;
}
