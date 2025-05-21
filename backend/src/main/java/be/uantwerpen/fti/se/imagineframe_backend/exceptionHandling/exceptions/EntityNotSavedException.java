package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntityNotSavedException extends RuntimeException {
    private String entityName;
    private String entityId;

    public String getMessage() {
        return String.format("The %s was not found: %s", entityName, entityId);
    }
}
