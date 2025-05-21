package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.IdentifierException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.PasswordException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Registration;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.GroupGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RegistrationServiceTest {

    private RegistrationService registrationService;
    private RegistrationRepository registrationRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;
    private ModelMapper modelMapper;

    private Registration validRequest;

    @BeforeEach
    void setup() {
        registrationRepository = mock(RegistrationRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = mock(UserService.class);
        modelMapper = mock(ModelMapper.class);

        registrationService = new RegistrationService(
                registrationRepository, passwordEncoder, userService, modelMapper);

        validRequest = new Registration(
                "frank.dew@mail.com", "Frank.Dew", "Moon", "Moon", "Frank", "Dewinne");
    }

    @Test
    void testSaveValidRegistrationRequest_success() {
        when(passwordEncoder.encode("Moon")).thenReturn("encoded");
        when(registrationRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Registration saved = registrationService.saveRegistration(validRequest);

        assertEquals("encoded", saved.getPassword());
        assertNull(saved.getAccepted());
        verify(registrationRepository).save(any());
    }

    @Test
    void testSaveRegistration_emptyPassword_throwsPasswordException() {
        validRequest.setPassword("");
        validRequest.setRepeatPassword("");

        assertThrows(PasswordException.class,
                () -> registrationService.saveRegistration(validRequest));
    }

    @Test
    void testSaveRegistration_passwordMismatch_throwsPasswordException() {
        validRequest.setRepeatPassword("Mismatch");

        assertThrows(PasswordException.class,
                () -> registrationService.saveRegistration(validRequest));
    }

    @Test
    void testSaveRegistration_invalidEmail_throwsIdentifierException() {
        validRequest.setEmail("not-an-email");

        assertThrows(IdentifierException.class,
                () -> registrationService.saveRegistration(validRequest));
    }

    @Test
    void testSaveRegistration_emptyUsername_throwsIdentifierException() {
        validRequest.setUsername("   ");
        assertThrows(IdentifierException.class,
                () -> registrationService.saveRegistration(validRequest));
    }

    @Test
    void testAcceptRegistration_createsUserAndSetsAccepted() {
        GroupGetDto group1 = new GroupGetDto(); group1.setId(1L);
        GroupGetDto group2 = new GroupGetDto(); group2.setId(2L);
        UserEditDto dto = new UserEditDto();
        User newUser = new User();

        when(modelMapper.map(validRequest, UserEditDto.class)).thenReturn(dto);
        when(userService.updateUserInformation(any(), eq(dto))).thenReturn(newUser);

        registrationService.acceptRegistration(validRequest, List.of(group1, group2));

        assertTrue(validRequest.getAccepted());
        verify(userService).saveUser(any(User.class));
    }

    @Test
    void testDeclineRegistration_setsAcceptedFalseAndSaves() {
        registrationService.declineRegistration(validRequest);
        verify(registrationRepository).save(validRequest);
        assertFalse(validRequest.getAccepted());
    }

    @Test
    void testDeleteRegistrationRequest_callsDeleteById() {
        registrationService.deleteRegistration(1L);
        verify(registrationRepository).deleteById(1L);
    }

    @Test
    void testSaveRegistration_repositoryThrows_throwsEntityNotSavedException() {
        when(passwordEncoder.encode("Moon")).thenReturn("encoded");
        when(registrationRepository.save(any())).thenThrow(new OptimisticLockingFailureException("fail"));

        assertThrows(EntityNotSavedException.class,
                () -> registrationService.saveRegistration(validRequest));
    }

    @Test
    void deleteAllDeclinedRegistrations_shouldReturnDeletedCount() {
        // Arrange
        when(registrationRepository.deleteByAccepted(false)).thenReturn(3L);

        // Act
        Long deletedCount = registrationService.deleteAllDeclinedRegistrations();

        // Assert
        assertEquals(3L, deletedCount);
        verify(registrationRepository, times(1)).deleteByAccepted(false);
    }

    @Test
    void deleteAllAcceptedRegistrations_shouldReturnDeletedCount() {
        // Arrange
        when(registrationRepository.deleteByAccepted(true)).thenReturn(5L);

        // Act
        Long deletedCount = registrationService.deleteAllAcceptedRegistrations();

        // Assert
        assertEquals(5L, deletedCount);
        verify(registrationRepository, times(1)).deleteByAccepted(true);
    }

    @Test
    void deleteAllDeclinedRegistrations_shouldThrowEntityNotSavedException_onFailure() {
        // Arrange
        when(registrationRepository.deleteByAccepted(false))
                .thenThrow(new OptimisticLockingFailureException("DB error"));

        // Act & Assert
        EntityNotSavedException thrown = assertThrows(EntityNotSavedException.class,
                () -> registrationService.deleteAllDeclinedRegistrations());

        assertEquals("User", thrown.getEntityName());
        assertEquals("declined", thrown.getEntityId());
    }

}
