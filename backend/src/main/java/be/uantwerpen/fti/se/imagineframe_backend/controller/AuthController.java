package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.LoginCredentials;
import be.uantwerpen.fti.se.imagineframe_backend.security.JWTUtil;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API for user authentication and token generation")
public class AuthController {
    @Value("${frontend_url}")
    private String frontendUrl;

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authManager;
    Logger logger = Logger.getLogger(AuthController.class.getName());

    public AuthController(UserService userService, JWTUtil jwtUtil, AuthenticationManager authManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authManager = authManager;
    }

    @Operation(summary = "Authenticate user")
    @PostMapping("/login")
    public Map<String, Object> loginHandler(@RequestBody LoginCredentials body) {
        logger.info("POST: /login");
        try {
            // Try and find user in repository
            User user = this.userService.findUser(body.getIdentifier());

            // Create authentication based on user id
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(user.getId(), body.getPassword());
            authManager.authenticate(authInputToken); // Calls the "SEUserDetailService.loadUserByUsername" from SecurityFilterChain

            // Generate the JWT token based on the user
            String token = jwtUtil.generateToken(user);

            return Collections.singletonMap("jwt-token", token);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No user found");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }
    }
}
