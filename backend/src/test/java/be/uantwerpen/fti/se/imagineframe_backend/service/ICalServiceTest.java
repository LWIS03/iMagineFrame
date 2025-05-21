package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ICalServiceTest {

    @InjectMocks
    private ICalService iCalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertEventsToICal_BasicEvent() throws Exception {
        Event event = new Event();
        event.setName("Test Event");
        event.setDescription("Test Description");
        event.setLocation("Test Location");
        event.setStartdate(LocalDateTime.of(2025, 5, 15, 10, 0));
        event.setEnddate(LocalDateTime.of(2025, 5, 15, 11, 0));

        User owner = new User();
        owner.setUsername("testuser");
        event.setOwner(owner);

        java.lang.reflect.Field idField = Event.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(event, 1L);
        List<Event> events = new ArrayList<>();
        events.add(event);
        String ical = iCalService.convertEventsToICal(events);

        assertTrue(ical.startsWith("BEGIN:VCALENDAR"));
        assertTrue(ical.contains("VERSION:2.0"));
        assertTrue(ical.contains("SUMMARY:Test Event"));
        assertTrue(ical.contains("DTSTART:20250515T100000"));
        assertTrue(ical.contains("DTEND:20250515T110000"));
        assertTrue(ical.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    void testConvertEventsToICal_EmptyList() {
        List<Event> events = new ArrayList<>();
        String ical = iCalService.convertEventsToICal(events);
        assertTrue(ical.startsWith("BEGIN:VCALENDAR"));
        assertTrue(ical.contains("VERSION:2.0"));
        assertFalse(ical.contains("BEGIN:VEVENT"));
        assertTrue(ical.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    void testConvertEventsToICal_MultipleEvents() throws Exception {
        List<Event> events = new ArrayList<>();
        Event event1 = new Event();
        event1.setName("Event 1");
        event1.setDescription("Description 1");
        event1.setLocation("Location 1");
        event1.setStartdate(LocalDateTime.of(2025, 5, 15, 10, 0));
        event1.setEnddate(LocalDateTime.of(2025, 5, 15, 11, 0));

        User owner1 = new User();
        owner1.setUsername("testuser");
        event1.setOwner(owner1);
        java.lang.reflect.Field idField1 = Event.class.getDeclaredField("id");
        idField1.setAccessible(true);
        idField1.set(event1, 1L);

        Event event2 = new Event();
        event2.setName("Event 2");
        event2.setDescription("Description 2");
        event2.setLocation("Location 2");
        event2.setStartdate(LocalDateTime.of(2025, 5, 16, 13, 0));
        event2.setEnddate(LocalDateTime.of(2025, 5, 16, 15, 0));

        User owner2 = new User();
        owner2.setUsername("testuser");
        event2.setOwner(owner2);
        idField1.set(event2, 2L);
        events.add(event1);
        events.add(event2);
        String ical = iCalService.convertEventsToICal(events);

        assertTrue(ical.contains("SUMMARY:Event 1"));
        assertTrue(ical.contains("SUMMARY:Event 2"));

        int eventCount = 0;
        int index = 0;
        String searchString = "BEGIN:VEVENT";
        while ((index = ical.indexOf(searchString, index)) != -1) {
            eventCount++;
            index += searchString.length();
        }
        assertEquals(2, eventCount);
    }
}