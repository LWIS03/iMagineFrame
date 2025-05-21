package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.EventParticipation;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceAttendanceTest {
    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipationRepository participationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    private Event event;
    private User user;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() throws Exception {
        closeable = MockitoAnnotations.openMocks(this);

        event = new Event();
        var idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, 1L);
        event.setName("Test Event");

        user = new User();
        var userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(user, 1L);
        user.setEmail("test@test.com");

        TestingAuthenticationToken auth = new TestingAuthenticationToken("1", "password", "event_write");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userService.findUser("1")).thenReturn(user);
    }

    @AfterEach
    void tearDown() throws Exception {
        SecurityContextHolder.clearContext();
        closeable.close();
    }

    @Test
    void testConfirmAttendance() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertTrue(eventService.confirmAttendance(1L, user));
        verify(participationRepository).save(any(EventParticipation.class));
    }

    @Test
    void testDenyAttendance() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertTrue(eventService.denyAttendance(1L, user));
        verify(participationRepository).save(any(EventParticipation.class));
    }

    @Test
    void testGetAttendance_WhenParticipating() {
        var participation = new EventParticipation();
        participation.setAttend(true);
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.of(participation));

        assertTrue(eventService.getAttendance(1L, user));
    }

    @Test
    void testGetAttendance_WhenNotParticipating() {
        var participation = new EventParticipation();
        participation.setAttend(false);
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.of(participation));

        assertFalse(eventService.getAttendance(1L, user));
    }

    @Test
    void testGetAttendance_WhenNoRecord() {
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertFalse(eventService.getAttendance(1L, user));
    }

    @Test
    void testGetParticipants() throws Exception {
        var p1 = new EventParticipation();
        var p2 = new EventParticipation();
        var user1 = new User();
        var user2 = new User();
        var f = User.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(user1, 1L);
        f.set(user2, 1L);
        p1.setUser(user1);
        p1.setAttend(true);
        p2.setUser(user2);
        p2.setAttend(false);

        when(participationRepository.findByEventId(1L)).thenReturn(List.of(p1, p2));

        var participants = eventService.getParticipants(1L,user);
        assertEquals(1, participants.size());
        assertEquals(1L, participants.get(0).getId());
    }

    @Test
    void testGetParticipantsAlphabetical() throws Exception {
        var p1 = new EventParticipation();
        var p2 = new EventParticipation();
        var p3 = new EventParticipation();

        var user1 = new User();
        var user2 = new User();
        var user3 = new User();

        var f = User.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(user1, 1L);
        f.set(user2, 2L);
        f.set(user3, 3L);

        user1.setFirstName("FirstnameA");
        user1.setLastName("LastnameA");
        user2.setFirstName("FirstnameB");
        user2.setLastName("LastnameB");
        user3.setFirstName("FirstnameC");
        user3.setLastName("LastnameC");

        p1.setUser(user1);
        p1.setAttend(true);
        p2.setUser(user2);
        p2.setAttend(true);
        p3.setUser(user3);
        p3.setAttend(true);

        when(participationRepository.findByEventId(1L)).thenReturn(List.of(p1,p2,p3));

        var participants = eventService.getParticipantsAlphabetical(1L,user);
        assertEquals(3, participants.size());
        assertEquals("LastnameA", participants.get(0).getLastName());
        assertEquals("LastnameB", participants.get(1).getLastName());
        assertEquals("FirstnameB", participants.get(1).getFirstName());
        assertEquals("LastnameC", participants.get(2).getLastName());
        assertEquals("FirstnameC", participants.get(2).getFirstName());
    }

    @Test
    void testGetCount() {
        var p1 = new EventParticipation();
        var p2 = new EventParticipation();
        var user1 = new User();
        var user2 = new User();
        p1.setUser(user1);
        p1.setAttend(true);
        p2.setUser(user2);
        p2.setAttend(true);

        when(participationRepository.findByEventId(1L)).thenReturn(List.of(p1, p2));

        assertEquals(2, eventService.getCount(1L,user));
    }
}