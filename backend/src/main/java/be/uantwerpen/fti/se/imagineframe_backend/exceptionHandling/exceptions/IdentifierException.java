package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class IdentifierException extends RuntimeException {
    private String description;

    public String getMessage() {
        return description;
    }
}
