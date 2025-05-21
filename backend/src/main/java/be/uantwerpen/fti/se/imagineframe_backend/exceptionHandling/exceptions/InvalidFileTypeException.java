package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions;

public class InvalidFileTypeException extends RuntimeException {
    private final String allowedTypes;
    private final String filename;

    public InvalidFileTypeException(String filename, String allowedTypes) {
        this.filename = filename;
        this.allowedTypes = allowedTypes;
    }

    @Override
    public String getMessage() {
        return String.format("'%s' has a invalid type, only %s files are allowed", filename, allowedTypes);
    }
}
