package be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.*;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // This exception happens when a null was provided where this is not allowed
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(final NoSuchElementException e) {
        logger.error("One of the arguments is not provided", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // This exception happens when an entity could not be found
    @ExceptionHandler(EntityNotSavedException.class)
    public ResponseEntity<String> handleEntityNotSavedException(final EntityNotSavedException e) {
        logger.error("EntityNotSaved: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // This exception happens when an entity could not be found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(final EntityNotFoundException e) {
        logger.error("EntityNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    // This exception happens when an entity could not be found
    @ExceptionHandler(PasswordException.class)
    public ResponseEntity<String> handlePasswordException(final PasswordException e) {
        logger.error("PasswordException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    // Already implemented the exception handling, but no validation is (yet) in place
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // This exception is thrown by the SEUserDetailService whenever the user is not found.
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(final UsernameNotFoundException e) {
        logger.error("UsernameNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IdentifierException.class)
    public ResponseEntity<String> handleIdentifierException(final IdentifierException e) {
        logger.error("Identifier: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    //This exception is thrown when the token cannot be created due to invalid claims, missing required fields, or other internal issues.
    @ExceptionHandler(JWTCreationException.class)
    public ResponseEntity<String> handleJWTCreationException(final JWTCreationException e) {
        logger.error("JWTCreationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(("Could not create JWT because of invalid claims, missing or incorrect format"));
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<String> handleJWTVerificationException(final JWTVerificationException e) {
        logger.error("JWTVerificationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT Token");
    }

    // Make sure ResponseStatusExceptions are not changed
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(final ResponseStatusException e) {
        logger.error("ResponseStatusException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
    }

    // exception is thrown when a type other than png or jpg is uploaded as photo
    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<String> handleInvalidFileTypeException(final InvalidFileTypeException e) {
        logger.error("Invalid file type: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(BatchExpiredException.class)
    public ResponseEntity<String> handleBatchExpiredException(final BatchExpiredException e) {
        logger.error("BatchExpiredException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
    }

    @ExceptionHandler(EntityWithTagNotFoundException.class)
    public ResponseEntity<String> handleEntityWithTagNotFoundException(final EntityWithTagNotFoundException e) {
        logger.error("EntityWithTagNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(final Exception e) {
        logger.error("An unexpected error occurred: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
