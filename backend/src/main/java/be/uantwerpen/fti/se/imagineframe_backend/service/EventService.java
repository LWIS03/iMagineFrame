package be.uantwerpen.fti.se.imagineframe_backend.service;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotSavedException;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.EventParticipation;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.EventEditDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;
    private final UserService userService;

    public EventService(EventRepository eventRepository, EventParticipationRepository participationRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.participationRepository = participationRepository;
        this.userService = userService;
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public void saveEvent(Event event) {
        if (event.getOwner() == null) {
            throw new IllegalArgumentException("event owner cant be null");
        }
        try {
            eventRepository.save(event);
        } catch (IllegalArgumentException | OptimisticLockingFailureException e) {
            throw new EntityNotSavedException("event", String.valueOf(event.getId()));
        }
    }

    public Event updateEventInformation(Event event, EventEditDto eventDto) {

        if (eventDto.getStartdate() != null && eventDto.getEnddate() != null
                && eventDto.getEnddate().isBefore(eventDto.getStartdate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        if (eventDto.getName() != null) {
            event.setName(eventDto.getName());
        }
        if (eventDto.getStartdate() != null) {
            event.setStartdate(eventDto.getStartdate());
        }
        if (eventDto.getEnddate() != null) {
            event.setEnddate(eventDto.getEnddate());
        }
        if (eventDto.getDescription() != null) {
            event.setDescription(eventDto.getDescription());
        }
        if (eventDto.getLocation() != null) {
            event.setLocation(eventDto.getLocation());
        }

        if (eventDto.getImageUrl() != null) {
            event.setImageUrl(eventDto.getImageUrl());
        }

        if (eventDto.getLabel() != null) {
            event.setLabel(eventDto.getLabel());
        }
        event.setPublic(eventDto.isPublic());
        return event;
    }

    public List<Event> findAll() {
        return (List<Event>) eventRepository.findAll();
    }

    public List<Event> findFilteredEvents(EventLabel label, LocalDateTime startDate, LocalDateTime endDate, String location) {
        List<Event> filteredEvents = findAll();
        //location filter
        if (location != null && !location.isEmpty()) {
            filteredEvents = filteredEvents.stream().filter(event -> event.getLocation().toLowerCase().contains(location.toLowerCase())).toList();
        }

        //label filter
        if (label != null) {
            filteredEvents = filteredEvents.stream().filter(event -> event.getLabel() == label).toList();
        }

        //date filter
        if (startDate != null && endDate != null) {
            filteredEvents = filteredEvents.stream().filter(event -> !event.getStartdate().isBefore(startDate) && !event.getStartdate().isAfter(endDate)).toList();
        }
        return filteredEvents;
    }

    //functions regarding event participation

    public boolean confirmAttendance(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("event", String.valueOf(eventId)));
        EventParticipation part = participationRepository.findByEventIdAndUserId(eventId,user.getId()).orElse(new EventParticipation());

        part.setEvent(event);
        part.setUser(user);
        part.setAttend(true);
        participationRepository.save(part);
        return true;
    }

    public boolean denyAttendance(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("event", String.valueOf(eventId)));
        EventParticipation part = participationRepository.findByEventIdAndUserId(eventId, user.getId()).orElse(new EventParticipation());
        part.setEvent(event);
        part.setUser(user);
        part.setAttend(false);
        participationRepository.save(part);
        return true;
    }

    public boolean getAttendance(Long eventId, User user) {
        return participationRepository.findByEventIdAndUserId(eventId,user.getId()).map(EventParticipation::isAttend).orElse(false);
    }

    public List<User> getParticipants(Long eventId, User currentUser) {
        boolean hasEventWritePrivilege;
        if (currentUser != null) {
            hasEventWritePrivilege = currentUser.getGroups().stream().flatMap(g -> g.getPrivileges().stream()).anyMatch(p -> p.getName().equals("event_write"));
        } else {
            hasEventWritePrivilege = false;
        }
        return participationRepository.findByEventId(eventId).stream().filter(EventParticipation::isAttend).map(EventParticipation::getUser).filter(user -> {
                    if (hasEventWritePrivilege || user.getId() == currentUser.getId()) {
                        return true;
                    }
                    switch (user.getPrivacyLevel()) {
                        case PUBLIC:
                            return true;
                        case IMAGINEERS_ONLY:
                            return currentUser.getGroups().stream().anyMatch(g -> g.getName().equals("iMagineer"));
                        case PRIVATE:
                            return false;
                        default:
                            return true;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<User> getParticipantsAlphabetical(Long eventId, User currentUser) {
        List<User> filteredParticipants = getParticipants(eventId,currentUser);
        return filteredParticipants.stream().sorted(Comparator.comparing(User::getLastName).thenComparing(User::getFirstName)).collect(Collectors.toList());
    }

    public int getCount(Long eventId, User currentUser) {
        return getParticipants(eventId, currentUser).size();
    }

    public int getTotalParticipantCount(Long eventId) {
        return (int) participationRepository.findByEventId(eventId).stream().filter(EventParticipation::isAttend).count();
    }

    public boolean isEventOwner(Long eventId, User user) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("event", String.valueOf(eventId)));
        return event.getOwner() != null && event.getOwner().getId() == user.getId();
    }

    public List<Event> findPublicEvents() {
        List<Event> allEvents = (List<Event>) eventRepository.findAll();
        return allEvents.stream().filter(Event::isPublic).collect(Collectors.toList());
    }

    public List<User> getPublicParticipants(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("event", String.valueOf(eventId)));
        if (!event.isPublic()) {
            return new ArrayList<>();
        }
        return participationRepository.findByEventId(eventId).stream().filter(EventParticipation::isAttend).map(EventParticipation::getUser).filter(user -> user.getPrivacyLevel() == PrivacyLevel.PUBLIC).collect(Collectors.toList());
    }

    public int getPublicParticipantsCount(Long eventId) {
        return getPublicParticipants(eventId).size();
    }

    public List<Event> getEventsUserParticipatesIn(User user) {
        return participationRepository.findByUserId(user.getId()).stream().filter(EventParticipation::isAttend).map(EventParticipation::getEvent).collect(Collectors.toList());
    }
}

