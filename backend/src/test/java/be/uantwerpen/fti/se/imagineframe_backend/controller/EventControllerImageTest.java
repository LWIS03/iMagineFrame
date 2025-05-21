package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.EventEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.EventService;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ICalService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@ExtendWith(MockitoExtension.class)
public class EventControllerImageTest {

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

    @InjectMocks
    private EventController eventController;

    private ObjectMapper objectMapper;
    private EventEditDto eventDto;
    private String eventJson;
    private Event mockEvent;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        eventDto = new EventEditDto();
        eventDto.setName("Test Event");
        eventDto.setDescription("Test Description");
        eventDto.setLocation("Test Location");
        eventDto.setStartdate(LocalDateTime.now());
        eventDto.setEnddate(LocalDateTime.now().plusHours(2));
        eventJson = objectMapper.writeValueAsString(eventDto);

        testUser = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(testUser, 1L);

        mockEvent = new Event();
        mockEvent.setOwner(testUser);
        java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockEvent, 1L);
        mockEvent.setName("Test Event");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("1");
        SecurityContextHolder.setContext(securityContext);

        when(userService.findUser("1")).thenReturn(testUser);

        eventController = new EventController(eventRepository, eventService, fileStorageService, userService, modelMapper, participationRepository,iCalService,calendarTokenRepository);
    }

    @Test
    void createEvent_JpgImage() throws Exception {
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class))).thenReturn(mockEvent);
        MockMultipartFile jpg = new MockMultipartFile("image", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image content".getBytes());
        when(fileStorageService.storeFile(any())).thenReturn("stored-filename.jpg");
        ResponseEntity<Event> response = eventController.createEvent(eventJson, jpg);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(fileStorageService).storeFile(jpg);
        verify(eventService).saveEvent(any(Event.class));
    }

    @Test
    void createEvent_PngImage() throws Exception {
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class))).thenReturn(mockEvent);
        MockMultipartFile png = new MockMultipartFile("image", "test.png", MediaType.IMAGE_PNG_VALUE, "test image content".getBytes());
        when(fileStorageService.storeFile(any())).thenReturn("stored-filename.png");
        ResponseEntity<Event> response = eventController.createEvent(eventJson, png);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(fileStorageService).storeFile(png);
        verify(eventService).saveEvent(any(Event.class));
    }

    @Test
    void createEvent_textFile() throws Exception {
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class))).thenReturn(mockEvent);
        MockMultipartFile txt = new MockMultipartFile("image", "test.txt", MediaType.TEXT_PLAIN_VALUE, "This is a text file".getBytes());
        when(fileStorageService.storeFile(any())).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST,"only images are allowed"));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            eventController.createEvent(eventJson, txt);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("only images are allowed"));
        verify(fileStorageService).storeFile(txt);
    }

    @Test
    void createEvent_NoImage() throws Exception {
        when(eventService.updateEventInformation(any(Event.class), any(EventEditDto.class))).thenReturn(mockEvent);
        ResponseEntity<Event> response = eventController.createEvent(eventJson, null);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(fileStorageService, never()).storeFile(any());
        verify(eventService).saveEvent(any(Event.class));
    }
}