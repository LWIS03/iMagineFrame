package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BatchExpiredException extends RuntimeException {
    private String batchId;

    public String getMessage() {
        return String.format("Batch with id %s is expired", batchId);
    }
}
