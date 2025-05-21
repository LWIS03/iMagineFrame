package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.model.CalendarToken;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.EventService;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ICalService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class EventControllerAdditionalTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventService eventService;

    @Mock
    private UserService userService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EventParticipationRepository participationRepository;

    @Mock
    private ICalService iCalService;

    @Mock
    private CalendarTokenRepository calendarTokenRepository;

    @InjectMocks
    private EventController eventController;

    private MockMvc mockMvc;
    private User testUser;
    private Event testEvent;

    @BeforeEach
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(testUser, 1L);
        testUser.setEmail("test@test.com");

        testEvent = new Event();
        Field idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testEvent, 1L);
        testEvent.setName("Test Event");
        testEvent.setOwner(testUser);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

        when(userService.findUser("1")).thenReturn(testUser);
        Field urlField = EventController.class.getDeclaredField("url");
        urlField.setAccessible(true);
        urlField.set(eventController, "http://localhost:8080");

        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    public void testGetEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.findAll()).thenReturn(events);

        List<Event> result = eventController.getEvents();

        assertEquals(events, result);
        verify(eventService).findAll();
    }

    @Test
    public void testGetFilteredEvents() {
        EventLabel label = EventLabel.PARTY;
        String startDate = "2025-01-01T12:00:00";
        String endDate = "2025-01-02T12:00:00";
        String location = "Brussels";
        List<Event> filteredEvents = Arrays.asList(testEvent);

        when(eventService.findFilteredEvents(eq(label), any(LocalDateTime.class), any(LocalDateTime.class), eq(location))).thenReturn(filteredEvents);

        List<Event> result = eventController.getFilteredEvents(label, startDate, endDate, location);

        assertEquals(filteredEvents, result);
        verify(eventService).findFilteredEvents(eq(label), any(LocalDateTime.class), any(LocalDateTime.class), eq(location));
    }

    @Test
    public void testGetPublicEvents() {
        List<Event> publicEvents = Arrays.asList(testEvent);
        when(eventService.findPublicEvents()).thenReturn(publicEvents);

        List<Event> result = eventController.getPublicEvents();

        assertEquals(publicEvents, result);
        verify(eventService).findPublicEvents();
    }

    @Test
    public void testGetEventById_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        ResponseEntity<Event> response = eventController.getEventById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testEvent, response.getBody());
        verify(eventRepository).findById(1L);
    }

    @Test
    public void testGetEventById_NotFound() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> eventController.getEventById(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Event not found", exception.getReason());
        verify(eventRepository).findById(999L);
    }

    @Test
    public void testDeleteEvent_Success() {
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventService.isEventOwner(1L, testUser)).thenReturn(true);

        ResponseEntity<Void> response = eventController.deleteEvent(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(eventRepository).deleteById(1L);
    }

    @Test
    public void testDeleteEvent_NotFound() {
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> eventController.deleteEvent(999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteEvent_NotOwner() {
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventService.isEventOwner(1L, testUser)).thenReturn(false);

        User otherUser = new User();
        Event event = new Event();
        event.setOwner(otherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> eventController.deleteEvent(1L));

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, exception.getStatusCode());
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testGenerateCalendarUrl() throws Exception {
        when(userService.findUser("1")).thenReturn(testUser);

        when(calendarTokenRepository.save(any(CalendarToken.class))).thenAnswer(invocation -> {
            CalendarToken savedToken = invocation.getArgument(0);
            Field tokenField = CalendarToken.class.getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(savedToken, "test-token");
            return savedToken;
        });

        ResponseEntity<Map<String, String>> response = eventController.generateCalendarUrl();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("url"));
        assertEquals("http://localhost:8080/events/calendar/test-token", response.getBody().get("url"));
        verify(userService).findUser("1");
        verify(calendarTokenRepository).deleteByUser(testUser);
        verify(calendarTokenRepository).save(any(CalendarToken.class));
    }
}