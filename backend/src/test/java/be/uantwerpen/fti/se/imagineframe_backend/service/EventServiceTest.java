package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.EventEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventParticipationRepository participationRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventEditDto testEventDto;
    private LocalDateTime now;
    private LocalDateTime future;
    private User testUser;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        now = LocalDateTime.now();
        future = now.plusHours(2);

        testUser = new User();
        java.lang.reflect.Field userIdField = User.class.getDeclaredField("id");
        userIdField.setAccessible(true);
        userIdField.set(testUser, 1L);

        testEvent = new Event();
        java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testEvent, 1L);
        testEvent.setName("event");
        testEvent.setDescription("description");
        testEvent.setLocation("location");
        testEvent.setStartdate(now);
        testEvent.setEnddate(future);
        testEvent.setLabel(EventLabel.HACKATHON);
        testEvent.setOwner(testUser);

        testEventDto = new EventEditDto();
        testEventDto.setName("updatedevent");
        testEventDto.setDescription("updateddescription");
        testEventDto.setLocation("updatedlocation");
        testEventDto.setStartdate(now);
        testEventDto.setEnddate(future);
        testEventDto.setLabel(EventLabel.CODING);
    }

    @Test
    void testSaveEvent() {
        when(eventRepository.save(testEvent)).thenReturn(testEvent);
        eventService.saveEvent(testEvent);
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void testSaveEvent_ThrowsException() {
        when(eventRepository.save(testEvent)).thenThrow(new OptimisticLockingFailureException("Test exception"));
        assertThrows(EntityNotSavedException.class, () -> {
            eventService.saveEvent(testEvent);
        });
    }

    @Test
    void testUpdateEventInformation() {
        Event updatedEvent = eventService.updateEventInformation(testEvent, testEventDto);
        assertEquals("updatedevent", updatedEvent.getName());
        assertEquals("updateddescription", updatedEvent.getDescription());
        assertEquals("updatedlocation", updatedEvent.getLocation());
        assertEquals(EventLabel.CODING, updatedEvent.getLabel());
    }

    @Test
    void testUpdateEventInformation_WithInvalidDates() {
        LocalDateTime earlierDate = now.minusHours(1);
        testEventDto.setEnddate(earlierDate);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            eventService.updateEventInformation(testEvent, testEventDto);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("End date must be after start date", exception.getReason());
    }

    @Test
    void testFindAll() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findAll();
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
    }

    @Test
    void testFindFilteredEvents_NoFilters() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findFilteredEvents(null, null, null, null);
        assertEquals(1, result.size());
        assertEquals(testEvent, result.get(0));
    }

    @Test
    void testFindFilteredEvents_WithLabel() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findFilteredEvents(EventLabel.HACKATHON, null, null, null);
        assertEquals(1, result.size());
        result = eventService.findFilteredEvents(EventLabel.PARTY, null, null, null);
        assertEquals(0, result.size());
    }

    @Test
    void testFindFilteredEvents_WithDateRange() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findFilteredEvents(null, now.minusHours(1), future.plusHours(1), null);
        assertEquals(1, result.size());
        result = eventService.findFilteredEvents(null, now.minusDays(2), now.minusDays(1), null);
        assertEquals(0, result.size());
        result = eventService.findFilteredEvents(null, future.plusDays(1), future.plusDays(2), null);
        assertEquals(0, result.size());
    }

    @Test
    void testFindFilteredEvents_WithLocation() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findFilteredEvents(null, null, null, "location");
        assertEquals(1, result.size());

        result = eventService.findFilteredEvents(null, null, null, "no");
        assertEquals(0, result.size());
    }

    @Test
    void testFindFilteredEvents_WithAllFilters() {
        List<Event> events = new ArrayList<>();
        events.add(testEvent);
        when(eventRepository.findAll()).thenReturn(events);
        List<Event> result = eventService.findFilteredEvents(EventLabel.HACKATHON, now.minusHours(1), future.plusHours(1), "location");
        assertEquals(1, result.size());
        result = eventService.findFilteredEvents(EventLabel.PARTY, now.minusHours(1), future.plusHours(1), "location");
        assertEquals(0, result.size());
    }
}