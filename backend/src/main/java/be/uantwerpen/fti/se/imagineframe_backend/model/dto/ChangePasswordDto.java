package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordDto {
    private String oldPassword;
    private String newPassword;
    private String newPasswordRepeated;

}
