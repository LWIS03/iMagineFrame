package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntityNotFoundException extends RuntimeException {
    private String entityName;
    private String entityId;

    public String getMessage() {
        return String.format("%s not found: %s",  entityName, entityId);
    }
}
