package be.uantwerpen.fti.se.imagineframe_backend.controller;

import aj.org.objectweb.asm.commons.Remapper;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.model.CalendarToken;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.EventParticipation;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.EventEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.EventService;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ICalService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/events")
@Tag(name = "Event Management", description = "API for managing events and event participation")
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;
    private final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final EventParticipationRepository participationRepository;
    private final ICalService iCalService;
    private final CalendarTokenRepository calendarTokenRepository;

    @Value("${base-url}")
    private String url;
    @Value("${files-path}")
    private String filesPath;


    public EventController(EventRepository eventRepository, EventService eventService, FileStorageService fileStorageService, UserService userService, ModelMapper modelMapper, EventParticipationRepository participationRepository, ICalService iCalService, CalendarTokenRepository calendarTokenRepository) {
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.participationRepository = participationRepository;
        this.iCalService = iCalService;
        this.calendarTokenRepository = calendarTokenRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Operation(summary = "Create a new event")
    @PostMapping("/new")
    @PreAuthorize("hasAuthority('event_create')")
    public ResponseEntity<Event> createEvent(@RequestPart("event") String eventJson, @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
        EventEditDto eventDto = objectMapper.readValue(eventJson, EventEditDto.class);
        Event newEvent = new Event();
        newEvent = eventService.updateEventInformation(newEvent, eventDto);

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        newEvent.setOwner(currentUser);

        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = fileStorageService.storeFile(imageFile);
            newEvent.setImageUrl(url + filesPath + filename);
        }

        eventService.saveEvent(newEvent);
        return ResponseEntity.status(HttpStatus.CREATED).body(newEvent);
    }

    @Operation(summary = "Get all events")
    @GetMapping
    public List<Event> getEvents() {
        logger.info("GET: /events");
        return eventService.findAll();
    }

    @Operation(summary = "Delete an event")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('event_create')")  // At minimum need event_create
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        Event event = eventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        boolean isOwner = event.getOwner() != null && event.getOwner().getId() == currentUser.getId();
        boolean isManager = currentUser.getGroups().stream().flatMap(g -> g.getPrivileges().stream()).anyMatch(p -> p.getName().equals("event_manage"));
        if (!isOwner && !isManager) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only the event creator or event managers can delete this event");
        }
        eventRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update an event")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('event_create')")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestPart("event") String eventJson, @RequestPart(value = "image", required = false) MultipartFile imageFile) throws Exception {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        Event existingEvent = eventRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        boolean isOwner = eventService.isEventOwner(id, currentUser);
        boolean isManager = currentUser.getGroups().stream().flatMap(g -> g.getPrivileges().stream()).anyMatch(p -> p.getName().equals("event_manage"));
        if (!isOwner && !isManager) {
            throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Only the event creator or event managers can edit this event");
        }
        EventEditDto eventDto = objectMapper.readValue(eventJson, EventEditDto.class);
        eventService.updateEventInformation(existingEvent, eventDto);
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = fileStorageService.storeFile(imageFile);
            existingEvent.setImageUrl(url + filesPath + filename);
        }
        eventService.saveEvent(existingEvent);
        return ResponseEntity.ok(existingEvent);
    }

    @Operation(summary = "Get filtered events")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('logon')")
    @ResponseBody
    public List<Event> getFilteredEvents(@RequestParam(required = false) EventLabel label, @RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate, @RequestParam(required = false) String location) {
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;

            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate);
            }

            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate);
            }

            return eventService.findFilteredEvents(label, start, end, location);
        } catch (Exception e) {
            logger.error("error filtering events: " + e.getMessage(), e);
            return eventService.findAll();
        }
    }

    @Operation(summary = "Get event by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        logger.info("GET: /events/{}", id);
        Event event = eventRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        return ResponseEntity.ok(event);
    }

    // event attendance endpoints

    //returns if we attend a particular event
    @Operation(summary = "Check attendance status")
    @GetMapping("/{id}/attendance")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Boolean> getAttendance(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        boolean status = eventService.getAttendance(id,currentUser);
        return ResponseEntity.ok(status);
    }

    //confirm our attendance, set it to true
    @Operation(summary = "Confirm attendance")
    @PostMapping("/{id}/attend")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Boolean> confirmAttendance(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);

        Optional<EventParticipation> existing = participationRepository.findByEventIdAndUserId(id, currentUser.getId());
        if (existing.isPresent() && existing.get().isAttend()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already attending this event");
        }

        boolean status = eventService.confirmAttendance(id, currentUser);
        return ResponseEntity.ok(status);
    }

    //deny our attendance, set it to false
    @Operation(summary = "Confirm attendance")
    @DeleteMapping("/{id}/attend")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Boolean> denyAttendance(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        boolean status = eventService.denyAttendance(id, currentUser);
        return ResponseEntity.ok(status);
    }

    //get all current participants
    @Operation(summary = "Get participants")
    @GetMapping("/{id}/participants")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<List<UserGetDto>> getParticipants(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        List<User> participants = eventService.getParticipants(id,currentUser);
        List<UserGetDto> dto = participants.stream().map(user -> modelMapper.map(user, UserGetDto.class)).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    //get all current participants
    @Operation(summary = "Get participants alphabetically")
    @GetMapping("/{id}/participants/alphabetical")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<List<UserGetDto>> getParticipantsAlphabetical(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        List<User> participants = eventService.getParticipantsAlphabetical(id,currentUser);
        List<UserGetDto> dto = participants.stream().map(user -> modelMapper.map(user, UserGetDto.class)).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get participant count")
    @GetMapping("/{id}/participants/count")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Integer> getParticipantCount(@PathVariable Long id) {
        int count = eventService.getTotalParticipantCount(id);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get participants by email")
    @GetMapping("/{id}/participants/email")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<List<UserGetDto>> getParticipantsByEmail(@PathVariable Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        List<User> participants = eventService.getParticipants(id,currentUser);
        participants.sort(Comparator.comparing(User::getEmail));
        List<UserGetDto> dto = participants.stream().map(user -> modelMapper.map(user, UserGetDto.class)).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get public events")
    @GetMapping("/public")
    public List<Event> getPublicEvents() {
        return eventService.findPublicEvents();
    }

    @Operation(summary = "Get public participants")
    @GetMapping("/{id}/public-participants")
    public ResponseEntity<List<UserGetDto>> getPublicParticipants(@PathVariable Long id) {
        List<User> participants = eventService.getPublicParticipants(id);
        List<UserGetDto> dto = participants.stream().map(user -> modelMapper.map(user, UserGetDto.class)).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Get public participant count")
    @GetMapping("/{id}/public-participants/count")
    public ResponseEntity<Integer> getPublicParticipantCount(@PathVariable Long id) {
        int count = eventService.getPublicParticipantsCount(id);
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Get current user's events")
    @GetMapping("/my-events")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<List<Event>> getMyEvents() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);

        List<Event> myEvents = eventService.getEventsUserParticipatesIn(currentUser);
        return ResponseEntity.ok(myEvents);
    }

    @Operation(summary = "Get current user's calendar")
    @GetMapping("/my-calendar")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<String> getMyEventsCalendar() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        List<Event> myEvents = eventService.getEventsUserParticipatesIn(currentUser);
        String iCalContent = iCalService.convertEventsToICal(myEvents);
        return ResponseEntity.ok().header("Content-Type", "text/calendar; charset=utf-8").header("Content-Disposition", "attachment; filename=\"my-events.ics\"").body(iCalContent);
    }


    @Operation(summary = "Generate calendar URL")
    @PostMapping("/my-calendar-url")
    @PreAuthorize("hasAuthority('logon')")
    public ResponseEntity<Map<String, String>> generateCalendarUrl() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.findUser(userId);
        calendarTokenRepository.deleteByUser(currentUser);
        CalendarToken calendarToken = new CalendarToken();
        calendarToken.setUser(currentUser);
        calendarTokenRepository.save(calendarToken);
        String url = this.url + "/events/calendar/" + calendarToken.getToken();
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(summary = "Get calendar by token")
    @GetMapping("/calendar/{token}")
    public ResponseEntity<String> getCalendarByToken(@PathVariable String token) {
        Optional<CalendarToken> tokenOpt = calendarTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        CalendarToken calendarToken = tokenOpt.get();
        User user = calendarToken.getUser();
        List<Event> myEvents = eventService.getEventsUserParticipatesIn(user);
        String iCalContent = iCalService.convertEventsToICal(myEvents);
        return ResponseEntity.ok().header("Content-Type", "text/calendar; charset=utf-8").header("Content-Disposition", "inline; filename=\"calendar.ics\"").body(iCalContent);
    }



}
