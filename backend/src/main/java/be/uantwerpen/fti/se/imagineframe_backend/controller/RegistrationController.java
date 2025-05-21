package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.IdentifierException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import be.uantwerpen.fti.se.imagineframe_backend.model.RegistrationResponse;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.RegistrationGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.RegistrationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/register")
@Tag(name = "Registration Management", description = "API for managing user registration requests")
@SecurityRequirement(name = "Bearer Authentication")
public class RegistrationController {
    private final RegistrationRepository registrationRepository;
    private final RegistrationService registrationService;

    private final ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(RegistrationController.class);

    public RegistrationController(RegistrationRepository registrationRepository, ModelMapper modelMapper, RegistrationService registrationService) {
        this.registrationRepository = registrationRepository;
        this.modelMapper = modelMapper;
        this.registrationService = registrationService;
    }

    @Operation(summary = "Get all registration requests",
            description = "Retrieves all pending and processed registration requests.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('registration_edit')")
    public List<RegistrationGetDto> findAllRegistrations() {
        return ((List<Registration>) registrationRepository.findAll())
                .stream()
                .map(req -> modelMapper.map(req, RegistrationGetDto.class))
                .toList();
    }

    @Operation(summary = "Create registration request",
            description = "Submits a new user registration request.")
    @PutMapping("/new")
    @ResponseStatus(HttpStatus.OK)
    public Registration newRegistration(@RequestBody Registration registration) {
        logger.info("PUT: /register/new");
        return registrationService.saveRegistration(registration);
    }

    @Operation(summary = "Get registration by ID",
            description = "Retrieves a specific registration request by its ID.")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('registration_edit')")
    public RegistrationGetDto findRegistrationById(@PathVariable Long id) {
        logger.info("GET: /register/{}", id);
        Registration regReq = registrationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Registration request", String.valueOf(id)));
        return modelMapper.map(regReq, RegistrationGetDto.class);
    }

    @Operation(summary = "Delete registration by ID",
            description = "Removes a specific registration request.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('registration_edit')")
    public ResponseEntity<String> deleteRegistrationById(@PathVariable Long id) {
        logger.info("DELETE: /register/{}", id);

        // Check if registration request is present
        registrationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Registration request",String.valueOf(id)));

        // Delete the registration request
        registrationService.deleteRegistration(id);
        return ResponseEntity.ok("Registration request " + id + " deleted");
    }

    @Operation(summary = "Process registration request",
            description = "Accepts or declines a registration request and assigns groups to new user.")
    @PostMapping("/process")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('registration_edit')")
    public ResponseEntity<String> acceptRegistration(@RequestBody RegistrationResponse registrationResponse) {
        logger.info("POST: /register/process");
        Long id = Long.valueOf(registrationResponse.getId());
        Registration registration = registrationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Registration request",String.valueOf(id)));

        // Check if not already processed
        if (registration.getAccepted() != null){
            return new ResponseEntity<>("Request has already been processed", HttpStatus.BAD_REQUEST);
        }

        try {
            if (registrationResponse.getAccepted()){
                if (registrationResponse.getGroups().isEmpty()){
                    throw new IllegalArgumentException("Assign at least 1 group to which the new user will belong");
                }
                registrationService.acceptRegistration(registration, registrationResponse.getGroups());
                return ResponseEntity.ok("Registration successfully accepted");
            } else {
                registrationService.declineRegistration(registration);
                return ResponseEntity.ok("Registration successfully declined");
            }
        } catch (IdentifierException e) {
            registrationService.declineRegistration(registration);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Delete all declined requests",
            description = "Removes all registration requests that were declined.")
    @DeleteMapping("/declined")
    @PreAuthorize("hasAuthority('registration_edit')")
    @Transactional
    public ResponseEntity<String> removeAllDeclinedRequests() {
        logger.info("DELETE: /register/declined");
        long n_deleted = registrationService.deleteAllDeclinedRegistrations();
        return ResponseEntity.ok("Deleted " + n_deleted + " requests");
    }

    @Operation(summary = "Delete all accepted requests",
            description = "Removes all registration requests that were accepted.")
    @DeleteMapping("/accepted")
    @PreAuthorize("hasAuthority('registration_edit')")
    @Transactional
    public ResponseEntity<String> removeAllAcceptedRequests() {
        logger.info("DELETE: /register/accepted");
        long n_deleted = registrationService.deleteAllAcceptedRegistrations();
        return ResponseEntity.ok("Deleted " + n_deleted + " requests");
    }
}
