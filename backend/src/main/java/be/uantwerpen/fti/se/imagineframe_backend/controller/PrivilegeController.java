package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.Privilege;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.PrivilegeGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.PrivilegeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/privileges")
@Tag(name = "Privilege Management", description = "API for retrieving system privileges")
@SecurityRequirement(name = "Bearer Authentication")
public class PrivilegeController {
    private final PrivilegeRepository privilegeRepository;
    private final Logger logger = LoggerFactory.getLogger(PrivilegeController.class);

    public PrivilegeController(PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    @Operation(summary = "Get all privileges", description = "Retrieves the complete list of system privileges with their IDs, names, and descriptions.")
    @GetMapping
    @PreAuthorize("hasAuthority('privileges_read')")
    public List<PrivilegeGetDto> getPrivileges() {
        logger.info("GET: /privileges");
        return ((List<Privilege>) privilegeRepository.findAll())
                .stream()
                .map(privilege -> new PrivilegeGetDto(privilege.getId(), privilege.getName(), privilege.getDescription()))
                .toList();
    }
}
