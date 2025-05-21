package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.IdentifierException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.PasswordException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.RegistrationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ModelMapper modelMapper;

    public RegistrationService(RegistrationRepository registrationRepository, PasswordEncoder passwordEncoder, UserService userService, ModelMapper modelMapper) {
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    public Registration  saveRegistration(Registration registration) {
        try {
            if (registration.getPassword().isEmpty()) {
                throw new PasswordException("Password is required");
            }
            // Check if both passwords are equal and remove the confirmedPassword if it is, otherwise send bad_request response
            if (!Objects.equals(registration.getPassword(), registration.getRepeatPassword())) {
                throw new PasswordException("Passwords do not match");
            }

            // TODO: Change this using a secure separate function for changing passwords
            String encodedPassword = passwordEncoder.encode(registration.getPassword());
            registration.setPassword(encodedPassword);
            registration.setRepeatPassword(encodedPassword);

            if (registration.getEmail().isEmpty()) {
                throw new IdentifierException("Email is required");
            }

            boolean isEmail = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(registration.getEmail())
                    .matches();

            if (!isEmail) {
                throw new IdentifierException("Email is not valid");
            }

            if (userService.userExists(registration.getEmail())) {
                throw new IdentifierException("Email already in use");
            }

            if (registration.getUsername() == null){
                try {
                    registration.setUsername(userService.generateUsername(registration.getFirstName(), registration.getLastName()));
                } catch (Exception e) {
                    throw new IdentifierException("Username or first and last name are required");
                }
            }
            else if (registration.getUsername().trim().isEmpty()) {
                throw new IdentifierException("Username cannot be only spaces");
            }

            if (userService.userExists(registration.getUsername())) {
                throw new IdentifierException("Username already in use");
            }

            return registrationRepository.save(registration);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("Registration request", registration.getEmail());
        }
    }

    public void deleteRegistration(Long id) {
        try {
            registrationRepository.deleteById(id);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    public void acceptRegistration(Registration registration, List<GroupGetDto> groups) {
        // Set registration to accepted
        registration.setAccepted(true);

        // Convert registration request into userEditDto
        UserEditDto newUser = modelMapper.map(registration, UserEditDto.class);
        newUser.setGroups(groups);

        // Add user
        userService.saveUser(userService.updateUserInformation(new User(), newUser));

    }

    public void declineRegistration(Registration registration) {
        // Set registration to declined
        registration.setAccepted(false);

        // Save changes
        registrationRepository.save(registration);
    }


    public Long deleteAllDeclinedRegistrations(){
        try {
            return registrationRepository.deleteByAccepted(false);
        } catch (OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("User","declined");
        }
    }

    public Long deleteAllAcceptedRegistrations(){
        try {
            return registrationRepository.deleteByAccepted(true);
        } catch (OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("User", "accepted");
        }
    }
}
