package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserEditDto {
    String firstName;
    String lastName;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    String email;
    String username;
    String password;
    String repeatPassword;
    PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;
    List<GroupGetDto> groups;
}
