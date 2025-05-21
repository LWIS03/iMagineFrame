package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.model.*;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class EventServicePrivacyTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipationRepository participationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    private User adminUser;
    private User iMagineerUser;
    private User regularUser;
    private User publicUser;
    private User imagineersOnlyUser;
    private User privateUser;
    private List<EventParticipation> participations;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);
        adminUser = new User();
        var userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(adminUser, 1L);

        Set<Group> adminGroups = new HashSet<>();
        Group adminGroup = new Group("Admin");
        Set<Privilege> adminPrivileges = new HashSet<>();
        Privilege eventWritePrivilege = new Privilege("event_write");
        adminPrivileges.add(eventWritePrivilege);
        adminGroup.setPrivileges(adminPrivileges);
        adminGroups.add(adminGroup);
        adminUser.setGroups(adminGroups);



        iMagineerUser = new User();
        userIdField.set(iMagineerUser, 2L);
        Set<Group> iMagineerGroups = new HashSet<>();
        Group iMagineerGroup = new Group("iMagineer");
        iMagineerGroups.add(iMagineerGroup);
        iMagineerUser.setGroups(iMagineerGroups);
        iMagineerGroup.setPrivileges(new HashSet<>());


        regularUser = new User();
        userIdField.set(regularUser, 3L);
        regularUser.setGroups(new HashSet<>());


        publicUser = new User();
        userIdField.set(publicUser, 4L);
        publicUser.setPrivacyLevel(PrivacyLevel.PUBLIC);

        imagineersOnlyUser = new User();
        userIdField.set(imagineersOnlyUser, 5L);
        imagineersOnlyUser.setPrivacyLevel(PrivacyLevel.IMAGINEERS_ONLY);

        privateUser = new User();
        userIdField.set(privateUser, 6L);
        privateUser.setPrivacyLevel(PrivacyLevel.PRIVATE);


        participations = new ArrayList<>();
        EventParticipation publicParticipation = new EventParticipation();
        publicParticipation.setUser(publicUser);
        publicParticipation.setAttend(true);
        participations.add(publicParticipation);
        EventParticipation imagineersOnlyParticipation = new EventParticipation();
        imagineersOnlyParticipation.setUser(imagineersOnlyUser);
        imagineersOnlyParticipation.setAttend(true);
        participations.add(imagineersOnlyParticipation);
        EventParticipation privateParticipation = new EventParticipation();
        privateParticipation.setUser(privateUser);
        privateParticipation.setAttend(true);
        participations.add(privateParticipation);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void testGetParticipants_AsAdminUser() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("1", "password", "event_write");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("1")).thenReturn(adminUser);
        when(participationRepository.findByEventId(1L)).thenReturn(participations);
        List<User> visibleParticipants = eventService.getParticipants(1L,adminUser);
        assertEquals(3, visibleParticipants.size());
        assertTrue(visibleParticipants.contains(publicUser));
        assertTrue(visibleParticipants.contains(imagineersOnlyUser));
        assertTrue(visibleParticipants.contains(privateUser));
    }

    @Test
    void testGetParticipants_AsIMagineerUser() {

        TestingAuthenticationToken auth = new TestingAuthenticationToken("2", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("2")).thenReturn(iMagineerUser);
        when(participationRepository.findByEventId(1L)).thenReturn(participations);
        List<User> visibleParticipants = eventService.getParticipants(1L,iMagineerUser);
        assertEquals(2, visibleParticipants.size());
        assertTrue(visibleParticipants.contains(publicUser));
        assertTrue(visibleParticipants.contains(imagineersOnlyUser));
    }

    @Test
    void testGetParticipants_AsRegularUser() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("3", "password");
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userService.findUser("3")).thenReturn(regularUser);
        when(participationRepository.findByEventId(1L)).thenReturn(participations);
        List<User> visibleParticipants = eventService.getParticipants(1L,regularUser);
        assertEquals(1, visibleParticipants.size());
        assertTrue(visibleParticipants.contains(publicUser));
    }
}