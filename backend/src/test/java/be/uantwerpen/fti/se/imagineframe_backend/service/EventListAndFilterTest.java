package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.controller.EventController;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class EventListAndFilterTest {

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
    private List<Event> testEvents;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventController = new EventController(eventRepository, eventService, fileStorageService, userService, modelMapper, participationRepository,iCalService,calendarTokenRepository);
        testEvents = new ArrayList<>();
        //event 1: 15 march 2025
        Event event1 = new Event();
        event1.setName("Coding camp");
        event1.setLocation("Brussel");
        event1.setDescription("Coding");
        event1.setLabel(EventLabel.CODING);
        event1.setStartdate(LocalDateTime.of(2025, 3, 15, 9, 0));
        event1.setEnddate(LocalDateTime.of(2025, 3, 16, 18, 0));

        //event 2: 10 april 2025
        Event event2 = new Event();
        event2.setName("Team Meeting");
        event2.setLocation("Antwerp");
        event2.setDescription("Meeting with team");
        event2.setLabel(EventLabel.MEETING);
        event2.setStartdate(LocalDateTime.of(2025, 4, 10, 10, 0));
        event2.setEnddate(LocalDateTime.of(2025, 4, 10, 11, 30));

        //event 3:20 may 2025
        Event event3 = new Event();
        event3.setName("Conference");
        event3.setLocation("Brugge");
        event3.setDescription("event");
        event3.setLabel(EventLabel.LEARNING);
        event3.setStartdate(LocalDateTime.of(2025, 5, 20, 9, 0));
        event3.setEnddate(LocalDateTime.of(2025, 5, 22, 17, 0));

        testEvents.add(event1);
        testEvents.add(event2);
        testEvents.add(event3);
        when(eventService.findAll()).thenReturn(testEvents);
    }

    @Test
    void testFilterEventsByDateRange() {
        List<Event> aprilEvents = List.of(testEvents.get(1));
        when(eventService.findFilteredEvents(null, LocalDateTime.parse("2025-04-01T00:00:00"), LocalDateTime.parse("2025-04-30T23:59:59"), null)).thenReturn(aprilEvents);
        List<Event> result = eventController.getFilteredEvents(null, "2025-04-01T00:00:00", "2025-04-30T23:59:59", null);
        assertEquals(1, result.size());
        assertEquals("Team Meeting", result.get(0).getName());
        assertEquals("Antwerp", result.get(0).getLocation());
        assertEquals(EventLabel.MEETING, result.get(0).getLabel());
    }

    @Test
    void testFilterEventsByLabel() {
        List<Event> meetingEvents = List.of(testEvents.get(1));
        when(eventService.findFilteredEvents(EventLabel.MEETING, null, null, null)).thenReturn(meetingEvents);

        List<Event> result = eventController.getFilteredEvents(EventLabel.MEETING, null, null, null);
        assertEquals(1, result.size());
        assertEquals("Team Meeting", result.get(0).getName());
        assertEquals(EventLabel.MEETING, result.get(0).getLabel());
    }

    @Test
    void testFilterEventsByLocation() {
        List<Event> antwerpEvents = List.of(testEvents.get(1));
        when(eventService.findFilteredEvents(null, null, null, "Antwerp")).thenReturn(antwerpEvents);
        List<Event> result = eventController.getFilteredEvents(null, null, null, "Antwerp");
        assertEquals(1, result.size());
        assertEquals("Team Meeting", result.get(0).getName());
        assertEquals("Antwerp", result.get(0).getLocation());
    }

    @Test
    void testFilterEventsByDateAndLabel() {
        List<Event> filteredEvents = List.of(testEvents.get(1));
        when(eventService.findFilteredEvents(EventLabel.MEETING, LocalDateTime.parse("2025-04-01T00:00:00"), LocalDateTime.parse("2025-04-30T23:59:59"), null)).thenReturn(filteredEvents);
        List<Event> result = eventController.getFilteredEvents(EventLabel.MEETING, "2025-04-01T00:00:00", "2025-04-30T23:59:59", null);
        assertEquals(1, result.size());
        assertEquals("Team Meeting", result.get(0).getName());
        assertEquals("Antwerp", result.get(0).getLocation());
        assertEquals(EventLabel.MEETING, result.get(0).getLabel());
    }

    @Test
    void testFilterEventsByAllParameters() {
        List<Event> filteredEvents = List.of(testEvents.get(1));
        when(eventService.findFilteredEvents(EventLabel.MEETING, LocalDateTime.parse("2025-04-01T00:00:00"), LocalDateTime.parse("2025-04-30T23:59:59"), "Antwerp")).thenReturn(filteredEvents);
        List<Event> result = eventController.getFilteredEvents(EventLabel.MEETING, "2025-04-01T00:00:00", "2025-04-30T23:59:59", "Antwerp");
        assertEquals(1, result.size());
        assertEquals("Team Meeting", result.get(0).getName());
        assertEquals("Antwerp", result.get(0).getLocation());
        assertEquals(EventLabel.MEETING, result.get(0).getLabel());
    }

    @Test
    void testFilterReturnsAllEventsWhenNoParams() {
        when(eventService.findFilteredEvents(null, null, null, null)).thenReturn(testEvents);
        List<Event> result = eventController.getFilteredEvents(null, null, null, null);
        assertEquals(3, result.size());
        assertEquals("Coding camp", result.get(0).getName());
        assertEquals("Team Meeting", result.get(1).getName());
        assertEquals("Conference", result.get(2).getName());
    }
}
