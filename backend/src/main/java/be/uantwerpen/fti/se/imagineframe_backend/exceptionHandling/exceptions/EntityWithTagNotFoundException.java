package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EntityWithTagNotFoundException extends RuntimeException {
    private String entityName;
    private String tagID;

    public String getMessage() {
        return String.format("No %s found with tag ID %s",entityName, tagID);
    }
}
