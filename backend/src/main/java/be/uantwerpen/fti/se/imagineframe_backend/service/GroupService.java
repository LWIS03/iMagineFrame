package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Group;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.GroupRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.PrivilegeRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {
    private final PrivilegeRepository privilegeRepository;

    @Value("${administrator_group_name}")
    private String adminGroupName;

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository, PrivilegeRepository privilegeRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.privilegeRepository = privilegeRepository;
    }

    public void saveGroup(Group group) {
        try {
            groupRepository.save(group);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("group", String.valueOf(group.getId()));
        }
    }

    public Group updateGroupInformation(Group oldGroup, GroupEditDto newGroup) {
        // To update
        // - name
        // - users
        // - privileges

        // Check if the old group is the admin group
        boolean isAdmin = oldGroup.getId() == 1L;

        if (newGroup.getName() != null) {
            if (isAdmin && !Objects.equals(newGroup.getName(), adminGroupName)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Administrator group name cannot be changed");
            }
            if (!isAdmin && Objects.equals(newGroup.getName(), adminGroupName)) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Group name cannot be changed");
            }
            if (newGroup.getName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group cannot be empty or all spaces");
            }
            oldGroup.setName(newGroup.getName().trim());
        } else if (oldGroup.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group name is required");
        }

        // Find if there is a symmetric difference between both sets
        boolean isDifferent = !newGroup.getUsers().containsAll(oldGroup.getUsers()) || !oldGroup.getUsers().containsAll(newGroup.getUsers());

        // update the group users if the newGroup contains a set with users and it is not the same as the old set of users.
        if (isDifferent) {
            // Create 2 sets for easier processing, currentUsers and updatedUsers
            Set<User> currentUsers = oldGroup.getUsers();
            Set<User> updatedUsers = new HashSet<>();
            for (UserGetDto user : newGroup.getUsers()) {
                updatedUsers.add(userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User", String.valueOf(user.getId()))));
            }

            // Get all users that need to be deleted from the group
            Set<User> toBeRemoved = new HashSet<>(currentUsers);
            toBeRemoved.removeAll(updatedUsers);

            // Get all users that need to be added to the group
            Set<User> toBeAdded = new HashSet<>(updatedUsers);
            toBeAdded.removeAll(currentUsers);

            // Check if all users from the admin group will be deleted. If so, throw error
            if ((currentUsers.size() - toBeRemoved.size() + toBeAdded.size()) <= 0 && isAdmin) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Cannot remove all users from admin group");
            }

            // Remove a current user if not in the new user Set.
            for (User user : toBeRemoved) {
                oldGroup.getUsers().remove(user);
                user.getGroups().remove(oldGroup);
            }

            // Add a new user if not already in the group
            for (User user : toBeAdded) {
                oldGroup.getUsers().add(user);
                user.getGroups().add(oldGroup);
            }
        }

        // Find if there is a symmetric difference between both sets
        isDifferent = !newGroup.getPrivileges().containsAll(oldGroup.getPrivileges()) || !oldGroup.getPrivileges().containsAll(newGroup.getPrivileges());

        if (isDifferent) {
            oldGroup.setPrivileges(newGroup.getPrivileges()
                    .stream()
                    .map(privilege -> privilegeRepository.findById(privilege.getId()).orElseThrow(() -> new EntityNotFoundException("Privilege", String.valueOf(privilege.getId()))))
                    .collect(Collectors.toSet())
            );
        }
        return oldGroup;
    }

    public void deleteUserFromGroup(long groupID, User user) throws EntityNotFoundException, ResponseStatusException {
        // Get the group
        Group group = groupRepository.findById(groupID).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group could not be found"));

        // Delete the user
        try {
            user.getGroups().remove(group);
            group.getUsers().remove(user);
            groupRepository.save(group);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("user", String.valueOf(user.getId()));
        }
    }

    public void addUser(long groupID, User user) throws EntityNotFoundException, ResponseStatusException {
        // Get the group
        Group group = groupRepository.findById(groupID).orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group could not be found"));

        // Add the user
        try {
            user.getGroups().add(group);
            group.getUsers().add(user);
            groupRepository.save(group);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("user", String.valueOf(user.getId()));
        }
    }

    public Group findGroup(long groupID) {
        try {
            return groupRepository.findById(groupID).get();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group could not be found");
        }
    }

    public void deleteByID(long groupID) {
        Group group = groupRepository.findById(groupID).orElseThrow(() -> new EntityNotFoundException("group", String.valueOf(groupID)));
        if (Objects.equals(group.getName(), adminGroupName)) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Administrator group cannot be deleted");
        }
        for (User user : group.getUsers()) {
            user.getGroups().remove(group);
            userRepository.save(user);
        }
        groupRepository.deleteById(groupID);
    }
}
