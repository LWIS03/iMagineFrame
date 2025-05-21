package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.GroupRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/groups")
@Tag(name = "Group Management", description = "API for creating, retrieving, updating, and deleting user groups")
@SecurityRequirement(name = "Bearer Authentication")
public class GroupController {
    private final GroupRepository groupRepository;
    private final GroupService groupService;
    private final ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(GroupController.class);

    public GroupController(GroupRepository groupRepository, GroupService groupService, ModelMapper modelMapper) {
        this.groupRepository = groupRepository;
        this.groupService = groupService;
        this.modelMapper = modelMapper;
    }

    @Operation(summary = "Get all groups", description = "Retrieves the complete list of user groups with their privileges and members.")
    @GetMapping
    @PreAuthorize("hasAuthority('groups_read')")
    public List<GroupGetDto> getGroups() {
        logger.info("GET: /groups");
        return ((List<Group>) groupRepository.findAll())
                .stream()
                .map(group -> modelMapper.map(group, GroupGetDto.class))
                .toList();
    }

    @Operation(summary = "Get group by ID", description = "Retrieves a specific group by its ID with detailed information.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('groups_read')")
    public GroupEditDto getGroupByID(@PathVariable Long id) {
        logger.info("GET: /users/{}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        return modelMapper.map(group, GroupEditDto.class);
    }

    @Operation(summary = "Update a group", description = "Updates an existing group's information including name, users, and privileges.")
    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('groups_write')")
    public void updateGroupByID(@PathVariable long id, @Valid @RequestBody GroupEditDto group) {
        logger.info("POST: /groups/" + id);
        Group existingGroup = groupRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        groupRepository.save(groupService.updateGroupInformation(existingGroup, group));
    }

    @Operation(summary = "Create a new group", description = "Creates a new user group with specified attributes.")
    @PutMapping("/new")
    @PreAuthorize("hasAuthority('groups_write')")
    public void createGroup(@Valid @RequestBody GroupEditDto group) {
        logger.info("PUT: /groups/new");

        Group newGroup = new Group();
        groupService.saveGroup(groupService.updateGroupInformation(newGroup, group));
    }

    @Operation(summary = "Delete a group", description = "Removes a group from the system. Admin group cannot be deleted.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('groups_write')")
    public void deleteGroupByID(@PathVariable Long id) {
        logger.info("DELETE: /groups/{}", id);
        if (groupRepository.findById(id).isPresent()) {
            groupService.deleteByID(id);
        } else {
            throw new EntityNotFoundException("group", String.valueOf(id));
        }
    }
}
