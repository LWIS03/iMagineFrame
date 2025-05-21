package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ChangePasswordDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name = "User Management", description = "API for managing user accounts and permissions")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final ModelMapper modelmapper;
    private final PasswordEncoder passwordEncoder;

    Logger logger = LoggerFactory.getLogger(UserController.class);

    // Pass jwtUtil in the constructor
    public UserController(UserRepository userRepository, UserService userService, ModelMapper modelmapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.modelmapper = modelmapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Create a new user",
            description = "Creates a new user account with the provided details.")
    @PutMapping("/new")
    @PreAuthorize("hasAuthority('user_edit')")
    public void createUser(@Valid @RequestBody UserEditDto user) throws ResponseStatusException {
        logger.info("PUT: /users/new");
        User newUser = new User();
        try {
            userRepository.save(userService.updateUserInformation(newUser, user));
        } catch (EntityNotSavedException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get all users",
            description = "Retrieves a list of all users in the system.")
    @GetMapping
    @PreAuthorize("hasAuthority('admin_read')")
    public Iterable<UserGetDto> getUsers() {
        logger.info("GET: /users");
        return ((List<User>) userRepository.findAll())
                .stream()
                .map(user -> modelmapper.map(user, UserGetDto.class))
                .toList();
    }

    @Operation(summary = "Get user by ID",
            description = "Retrieves a specific user's details by their ID.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {

        logger.info("GET: /users/{}", id);
        // 1) Find the currently logged-in user (me)
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = this.userService.findUser(loggedInUser);

        // 2) Check if the user has the admin_read permission
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminWrite = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin_read"));

        // 3) Determine if person can retrieve the details.
        if (me.getId() != id && !hasAdminWrite) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(modelmapper.map(user, UserGetDto.class));
    }

    @Operation(summary = "Update user",
            description = "Updates an existing user's account information.")
    @PostMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UserGetDto updateUser(@PathVariable long id, @Valid @RequestBody UserEditDto userDto) {
        logger.info("POST: /users/{}", id);

        // 1) Find the currently logged-in user (me)
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = this.userService.findUser(loggedInUser);

        // 2) Check if the user has the admin_write permission
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean hasAdminWrite = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("admin_write"));

        // 3) Determine if editing is allowed
        if (me.getId() == id || hasAdminWrite) {
            // (a) Editing your own account → allowed
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Change password to encoded password
            if (userDto.getPassword() != null) {
                userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            User updatedUser = userRepository.save(userService.updateUserInformation(existingUser, userDto));

            // Create the DTO to return
            return modelmapper.map(updatedUser, UserGetDto.class);
        } else {
            // (c) None of the above → 403 Forbidden
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot edit user");
        }
    }

    @Operation(summary = "Change password",
            description = "Allows a user to change their own password.")
    @PostMapping("/{id}/changePassword")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<String> changePassword(@PathVariable long id, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        logger.info("POST: /users/{}/changePassword", id);

        // 1) Find the currently logged-in user (me)
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.findUser(loggedInUser);

        if (me.getId() != id) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Not allowed to change password of other users");
        }

        userService.changePassword(me, changePasswordDto);
        return ResponseEntity.ok("Password changed");
    }

    @Operation(summary = "Force change password",
            description = "Allows administrators to change another user's password.")
    @PostMapping("/{id}/forceChangePassword")
    @PreAuthorize("hasAuthority('password_edit')")
    public ResponseEntity<String> forceChangePassword(@PathVariable long id, @Valid @RequestBody ChangePasswordDto changePasswordDto) {
        logger.info("POST: /users/{}/forceChangePassword", id);

        userService.forceChangePassword(id, changePasswordDto);
        return ResponseEntity.ok("Password changed");
    }

    @Operation(summary = "Delete user",
            description = "Removes a user account from the system.")
    @Transactional
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user_edit')")
    public ResponseEntity<Object> deleteUser(@PathVariable Long id) {
        logger.info("DELETE: /users/{}", id);

        // 1) Find the currently logged-in user (me)
        String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User me = userService.findUser(loggedInUser);

        if (me.getId() == id) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Not allowed to delete your own user");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User with ID " + id + " deleted");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
