package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.IdentifierException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.PasswordException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ChangePasswordDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GroupService groupService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GroupService groupService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.groupService = groupService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This function updates an old user with new user properties.
     * Additionally, it confirms that the email is unique and not empty and that a username is provided.
     *
     * @param oldUser The user for which the data will be changed.
     * @param newUser The user holding the new data.
     * @return The changed user
     */
    public User updateUserInformation(User oldUser, UserEditDto newUser) {
        // Check states of emails
        boolean oldUserHasEmail = oldUser.getEmail() != null && !Objects.requireNonNull(oldUser.getEmail()).trim().isEmpty();
        boolean newUserHasEmail = newUser.getEmail() != null && !Objects.requireNonNull(newUser.getEmail()).trim().isEmpty();

        // Check states of usernames
        boolean oldUserHasUsername = oldUser.getUsername() != null && !Objects.requireNonNull(oldUser.getUsername()).trim().isEmpty();
        boolean newUserHasUsername = newUser.getUsername() != null && !Objects.requireNonNull(newUser.getUsername()).trim().isEmpty();

        // Change email
        if (newUserHasEmail) {
            // If new user has email, and it is in database but not the same as old user -> throw error
            if (userRepository.findByEmail(newUser.getEmail()).isPresent() && !newUser.getEmail().equals(oldUser.getEmail())) {
                throw new IdentifierException("Email already in use");
            }
            // If new user has email, and it is not in database -> change it
            oldUser.setEmail(newUser.getEmail());

            // If new user has no email and old user has no email -> throw error
        } else if (!oldUserHasEmail) {
            throw new IdentifierException("Email is required");
        }
        // If new user has no email but old user has -> do nothing

        // Change first name
        if (newUser.getFirstName() != null) {
            oldUser.setFirstName(newUser.getFirstName());
        }

        // Change last name
        if (newUser.getLastName() != null) {
            oldUser.setLastName(newUser.getLastName());
        }

        // Change username
        if (newUserHasUsername) {
            // If new user has username, and it is in database but not the same as the old user -> throw error
            if (userRepository.findByUsername(newUser.getUsername()).isPresent() && !newUser.getUsername().equals(oldUser.getUsername())) {
                throw new IdentifierException("Username already in use");
            }

            // If new user has username, and it is not in database -> change it
            oldUser.setUsername(newUser.getUsername());
        } else if (!oldUserHasUsername) {
            try {
                String generatedUsername = this.generateUsername(oldUser.getFirstName(), oldUser.getLastName());
                oldUser.setUsername(generatedUsername);
            } catch (Exception e) {
                throw new IdentifierException("Username is required");
            }
        }

        // Check if the password needs to be updated -> newUser.getPassword != null && oldUser.getPassword == null
        if (oldUser.getPassword() == null && newUser.getPassword() != null) {
            // Check if new passwords match
            if (!newUser.getPassword().equals(newUser.getRepeatPassword())) {
                throw new PasswordException("Passwords should match");
            }

            // Check if new passwords are not empty
            if (newUser.getPassword().trim().isEmpty()) {
                throw new PasswordException("Password cannot be empty");
            }
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        } else if (oldUser.getPassword() == null) {
            throw new PasswordException("Password is required");
        }

        // Change groups
        if (newUser.getGroups() != null) {
            // Create 2 sets for easier processing, currentGroups and updatedGroups
            Set<Group> currentGroups = oldUser.getGroups();
            Set<Group> updatedGroups = new HashSet<>();
            for (GroupGetDto groupGetDto : newUser.getGroups()) {
                updatedGroups.add(groupService.findGroup(groupGetDto.getId()));
            }

            // Remove a current group if not in the new user Set.
            for (Group group : currentGroups) {
                if (!updatedGroups.contains(group)) {
                    groupService.deleteUserFromGroup(group.getId(), oldUser);
                }
            }

            // Add a new group if not already part of the user
            for (Group group : updatedGroups) {
                if (!oldUser.getGroups().contains(group)) {
                    groupService.addUser(group.getId(), oldUser);
                }
            }
        }
        // Set the privacy level to make user (in)visible on the participant list
        if (newUser.getPrivacyLevel() != null) {
            oldUser.setPrivacyLevel(newUser.getPrivacyLevel());
        }

        return oldUser;
    }

    /**
     * This function tries to generate a unique username by combining the last and first name of the user and adding a random value to it.
     * This name is then checked in the database for uniqueness.
     *
     * @param firstName The first name of the user
     * @param lastName The lastname of the user
     * @return The generated username
     */
    public String generateUsername(String firstName, String lastName) throws Exception {
        // If no username provided, create one from first and last name
        if (firstName!= null && lastName != null) {
            // Get random number between 0 and 100
            int randomNum = (int) (Math.random() * 101);
            String newUsername = lastName + "." + firstName.charAt(0) + randomNum;

            // Check if the username already exists
            Optional<User> userOptional = userRepository.findByUsername(newUsername);
            while (userOptional.isPresent()) {
                randomNum += 1;
                newUsername = lastName + "." + firstName.charAt(0) + randomNum;
                // Check if the username already exists
                userOptional = userRepository.findByUsername(newUsername);
            }
            return newUsername;

        } else {
            // Throw error if no username can be made
            throw new Exception();
        }
    }

    /**
     * Finds the user in the repository based on the identifier which can be:
     * - Its unique ID (Long)
     * - Its username (String)
     * - Its email (String)
     * The method wil either return the User or throw an exception.
     *
     * @param identifier the value on which to find the user
     * @return User
     */
    public User findUser(String identifier) throws UsernameNotFoundException {
        Optional<User> optUser;
        try {
            optUser = Optional.ofNullable(userRepository.findById(Long.parseLong(identifier)).orElseThrow(() -> new UsernameNotFoundException("Could not find user with id " + identifier)));
        } catch (NumberFormatException e) {
            // Checks if the given identifier is an email by using Regex: https://www.baeldung.com/java-email-validation-regex
            boolean isEmail = Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")
                    .matcher(identifier)
                    .matches();

            if (isEmail) {
                optUser = Optional.ofNullable(this.userRepository.findByEmail(identifier).orElseThrow(() -> new UsernameNotFoundException("Could not find user with email " + identifier)));
            } else {
                optUser = Optional.ofNullable(this.userRepository.findByUsername(identifier).orElseThrow(() -> new UsernameNotFoundException("Could not find user with username " + identifier)));
            }
        }

        return optUser.orElseThrow(() -> new UsernameNotFoundException("Could not find user with identifier " + identifier));
    }

    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("user", String.valueOf(user.getId()));
        }
    }

    public void deleteUser(Long id) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (optionalUser.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist");
            }

            User user = optionalUser.get();

            // Delete user from all groups
            for (Group group : user.getGroups()) {
                groupService.deleteUserFromGroup(group.getId(), user);
            }

            // Delete the user.
            userRepository.deleteById(id);

        } catch (UsernameNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist");
        }
    }

    public boolean userExists(String identifier) {
        try {
            findUser(identifier);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    public void changePassword(User requestingUser, ChangePasswordDto changePasswordDto) {
        // Check if the old password matches the current password
        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), requestingUser.getPassword())) {
            throw new PasswordException("Old password does not match");
        }

        // Check if both passwords are the same
        if (!Objects.equals(changePasswordDto.getNewPassword(), changePasswordDto.getNewPasswordRepeated())) {
            throw new PasswordException("New passwords does not match");
        }

        // Save the new encoded password
        requestingUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));

        // Save the changes
        userRepository.save(requestingUser);
    }

    public void forceChangePassword(long targetUserId, ChangePasswordDto changePasswordDto) {
        // Get the user
        Optional<User> targetUserOpt = userRepository.findById(targetUserId);

        if (targetUserOpt.isEmpty()) {
            throw new EntityNotFoundException("User", String.valueOf(targetUserId));
        }

        if (changePasswordDto.getNewPassword() == null || changePasswordDto.getNewPassword().trim().isEmpty()) {
            throw new PasswordException("New password is empty");
        }

        if (changePasswordDto.getNewPasswordRepeated() == null || changePasswordDto.getNewPasswordRepeated().trim().isEmpty()) {
            throw new PasswordException("Repeat password is empty");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getNewPasswordRepeated())) {
            throw new PasswordException("New passwords do not match");
        }

        User targetUser = targetUserOpt.get();

        // Save the new encoded password
        targetUser.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));

        // Save the changes
        userRepository.save(targetUser);
    }
}
