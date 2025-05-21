package be.uantwerpen.fti.se.imagineframe_backend;

import be.uantwerpen.fti.se.imagineframe_backend.model.*;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.*;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        //Convertors
        // Set<Privilege> -> Set<PrivilegeGetDto>
        Converter<Set<Privilege>, Set<PrivilegeGetDto>> privilegeConverter =
                mappingContext -> mappingContext.getSource() == null ? null :
                        mappingContext.getSource().stream()
                                .map(privilege -> new PrivilegeGetDto(
                                        privilege.getId(),
                                        privilege.getName(),
                                        privilege.getDescription()))
                                .collect(Collectors.toSet());

        // Convert Set<User> -> Set<UserGetDto> (But with EMPTY groups to avoid loop)
        Converter<Set<User>, Set<UserGetDto>> userConverter =
                mappingContext -> mappingContext.getSource() == null ? null :
                        mappingContext.getSource().stream()
                                .map(user -> new UserGetDto(
                                        user.getId(),
                                        user.getFirstName(),
                                        user.getLastName(),
                                        user.getUsername(),
                                        user.getEmail(),
                                        Set.of())) // Empty group set!
                                .collect(Collectors.toSet());

        // Convert Set<Group> -> Set<GroupGetDto> (But with EMPTY users and EMPTY privileges to avoid loop)
        Converter<Set<Group>, Set<GroupGetDto>> groupConverterBasic =
                mappingContext -> mappingContext.getSource() == null ? null :
                        mappingContext.getSource().stream()
                                .map(group -> new GroupGetDto(
                                        group.getId(),
                                        group.getName(),
                                        Set.of(),
                                        Set.of()))
                                .collect(Collectors.toSet());

        // Convert User -> UserGetDto for owner fields
        Converter<User, UserGetDto> ownerConverter = mappingContext -> {
            if (mappingContext.getSource() == null) {
                return null;
            }
            User owner = mappingContext.getSource();
            return new UserGetDto(
                    owner.getId(),
                    owner.getFirstName(),
                    owner.getLastName(),
                    owner.getUsername(),
                    owner.getEmail(),
                    Set.of()
            );
        };

        //Custom mappings
        // Group -> GroupGetDto
        TypeMap<Group, GroupGetDto> groupToGroupGetDto = modelMapper.createTypeMap(Group.class, GroupGetDto.class);
        groupToGroupGetDto.addMappings(mapper -> {
            mapper.map(Group::getId, GroupGetDto::setId);
            mapper.map(Group::getName, GroupGetDto::setName);
            mapper.using(privilegeConverter).map(Group::getPrivileges, GroupGetDto::setPrivileges);
            mapper.using(userConverter).map(Group::getUsers, GroupGetDto::setUsers);
        });

        // User -> UserGetDto
        TypeMap<User, UserGetDto> userToUserGetDto = modelMapper.createTypeMap(User.class, UserGetDto.class);
        userToUserGetDto.addMappings(mapper -> {
            mapper.map(User::getId, UserGetDto::setId);
            mapper.map(User::getFirstName, UserGetDto::setFirstName);
            mapper.map(User::getLastName, UserGetDto::setLastName);
            mapper.map(User::getEmail, UserGetDto::setEmail);
            mapper.map(User::getUsername, UserGetDto::setUsername);
            mapper.using(groupConverterBasic).map(User::getGroups, UserGetDto::setGroups);
        });

        // Registration -> RegistrationGetDto
        TypeMap<Registration, RegistrationGetDto> regToRegGetDto = modelMapper.createTypeMap(Registration.class, RegistrationGetDto.class);
        regToRegGetDto.addMappings(mapper -> {
            mapper.map(Registration::getId, RegistrationGetDto::setId);
            mapper.map(Registration::getUsername, RegistrationGetDto::setUsername);
            mapper.map(Registration::getEmail, RegistrationGetDto::setEmail);
            mapper.map(Registration::getAccepted, RegistrationGetDto::setAccepted);
            mapper.map(Registration::getDateCreated, RegistrationGetDto::setDateCreated);
            mapper.map(Registration::getDateUpdated, RegistrationGetDto::setDateUpdated);
            mapper.map(Registration::getFirstName, RegistrationGetDto::setFirstName);
            mapper.map(Registration::getLastName, RegistrationGetDto::setLastName);
        });

        // Registration -> UserEditDto
        TypeMap<Registration, UserEditDto> regToUserEditDto = modelMapper.createTypeMap(Registration.class, UserEditDto.class);
        regToUserEditDto.addMappings(mapper -> {
            mapper.map(Registration::getUsername, UserEditDto::setUsername);
            mapper.map(Registration::getEmail, UserEditDto::setEmail);
            mapper.map(Registration::getFirstName, UserEditDto::setFirstName);
            mapper.map(Registration::getLastName, UserEditDto::setLastName);
            mapper.map(Registration::getPassword, UserEditDto::setPassword);
        });

        modelMapper.getConfiguration().setFieldMatchingEnabled(false).setSkipNullEnabled(true).setImplicitMappingEnabled(false);

        TypeMap<Project, ProjectGetDto> projectMap = modelMapper.createTypeMap(Project.class, ProjectGetDto.class);
        projectMap.addMappings(m -> {
            m.map(Project::getId, ProjectGetDto::setId);
            m.map(Project::getName, ProjectGetDto::setName);
            m.map(Project::getDescription, ProjectGetDto::setDescription);
            m.map(Project::getMediaUrl, ProjectGetDto::setMediaUrl);
            m.map(Project::getStatus, ProjectGetDto::setStatus);
            m.map(Project::isPublic, ProjectGetDto::setPublic);
            m.using(ownerConverter).map(Project::getOwner, ProjectGetDto::setOwner);
        });

        TypeMap<ProjectJoinRequest, ProjectJoinRequestGetDto> projectJoinRequestMap = modelMapper.createTypeMap(ProjectJoinRequest.class, ProjectJoinRequestGetDto.class);
        projectJoinRequestMap.addMappings(m -> {
            m.map(ProjectJoinRequest::getId, ProjectJoinRequestGetDto::setId);
            m.map(src -> src.getUser().getId(), ProjectJoinRequestGetDto::setUserId);
            m.map(src -> src.getUser().getUsername(), ProjectJoinRequestGetDto::setUsername);
            m.map(src -> src.getProject().getId(), ProjectJoinRequestGetDto::setProjectId);
            m.map(src -> src.getProject().getName(), ProjectJoinRequestGetDto::setProjectName);
            m.map(ProjectJoinRequest::getDateCreated, ProjectJoinRequestGetDto::setDateCreated);
            m.map(ProjectJoinRequest::getDateUpdated, ProjectJoinRequestGetDto::setDateUpdated);
            m.map(ProjectJoinRequest::getAccepted, ProjectJoinRequestGetDto::setAccepted);
        });

        return modelMapper;
    }
}