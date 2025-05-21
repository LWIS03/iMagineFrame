package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.controller.EventController;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.EventEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EventCreationTests {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventService eventService;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EventParticipationRepository participationRepository;
    @Mock
    private ICalService iCalService;
    @Mock
    private CalendarTokenRepository calendarTokenRepository;

    private EventController eventController;
    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(testUser, 1L);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

        when(userService.findUser("1")).thenReturn(testUser);

        eventController = new EventController(eventRepository, eventService, fileStorageService, userService, modelMapper, participationRepository,iCalService,calendarTokenRepository);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Event e = new Event();
        e.setName("Test");
        e.setLabel(EventLabel.HACKATHON);
        e.setOwner(testUser);

        when(eventRepository.save(any(Event.class))).thenReturn(e);
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class))).thenReturn(e);
    }

    @Test
    void createEventValidDate() throws Exception {
        EventEditDto dto = new EventEditDto();
        dto.setName("Test");
        dto.setDescription("description");
        dto.setLocation("location");
        dto.setLabel(EventLabel.HACKATHON);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        dto.setEnddate(end);
        dto.setStartdate(start);

        String eventJson = objectMapper.writeValueAsString(dto);

        MockMultipartFile imageFile = null;
        ResponseEntity<Event> response = eventController.createEvent(eventJson, imageFile);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test", response.getBody().getName());
        assertEquals(EventLabel.HACKATHON, response.getBody().getLabel());
    }

    @Test
    void createEventInvalidDate() throws Exception {
        EventEditDto dto = new EventEditDto();
        dto.setName("Test");
        dto.setLocation("location");
        dto.setDescription("description");
        dto.setLabel(EventLabel.HACKATHON);
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);
        dto.setStartdate(start);
        dto.setEnddate(end);
        String eventJson = objectMapper.writeValueAsString(dto);
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be later than the startdate"));

        assertThrows(ResponseStatusException.class, () -> {
            eventController.createEvent(eventJson, null);
        });

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void createEventPastStartDate() throws Exception {
        EventEditDto dto = new EventEditDto();
        dto.setName("Test");
        dto.setLocation("location");
        dto.setDescription("description");
        dto.setLabel(EventLabel.HACKATHON);
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        dto.setStartdate(start);
        dto.setEnddate(end);
        String eventJson = objectMapper.writeValueAsString(dto);
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past"));

        assertThrows(ResponseStatusException.class, () -> {
            eventController.createEvent(eventJson, null);
        });

        verify(eventRepository, never()).save(any(Event.class));
    }
}