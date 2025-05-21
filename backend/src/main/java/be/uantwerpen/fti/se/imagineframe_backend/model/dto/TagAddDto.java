package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TagAddDto {
    @NotBlank(message = "Tag name is mandatory")
    private String name;
}
