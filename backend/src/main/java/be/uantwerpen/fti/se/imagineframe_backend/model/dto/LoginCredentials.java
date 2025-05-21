package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginCredentials {
    private String identifier;
    private String password;
}
