package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.exceptions.EntityNotFoundException;
import be.uantwerpen.fti.se.imagineframe_backend.model.Event;
import be.uantwerpen.fti.se.imagineframe_backend.model.EventParticipation;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.UserGetDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.CalendarTokenRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventParticipationRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.EventRepository;
import be.uantwerpen.fti.se.imagineframe_backend.service.EventService;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ICalService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerAttendanceTest {

    @Mock
    private EventService eventService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private FileStorageService fileStorageService;
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
        eventController = new EventController(eventRepository, eventService, fileStorageService, userService, modelMapper, participationRepository,iCalService,calendarTokenRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    @Test
    @WithMockUser(username = "1")
    public void testGetAttendance() throws Exception {
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventService.getAttendance(1L, testUser)).thenReturn(true);
        mockMvc.perform(get("/events/1/attendance")).andExpect(status().isOk()).andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"logon"})
    public void testConfirmAttendance() throws Exception {
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventService.confirmAttendance(1L, testUser)).thenReturn(true);
        mockMvc.perform(post("/events/1/attend")).andExpect(status().isOk()).andExpect(content().string("true"));

    }

    @Test
    @WithMockUser(username = "1", authorities = {"logon"})
    public void testDenyAttendance() throws Exception {
        when(userService.findUser(any())).thenReturn(testUser);
        when(eventService.denyAttendance(anyLong(), any(User.class))).thenReturn(true);
        mockMvc.perform(delete("/events/1/attend")).andExpect(status().isOk()).andExpect(content().string("true"));

    }

    @Test
    @WithMockUser(username = "1", authorities = {"event_write"})
    public void testGetParticipants() throws Exception {
        List<User> participants = new ArrayList<>();
        participants.add(testUser);
        List<UserGetDto> dtos = new ArrayList<>();
        UserGetDto dto = new UserGetDto();
        dto.setId(1L);
        dtos.add(dto);

        when(eventService.isEventOwner(eq(1L), any(User.class))).thenReturn(true);
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventService.getParticipants(eq(1L), any(User.class))).thenReturn(participants);
        when(modelMapper.map(any(User.class), eq(UserGetDto.class))).thenReturn(dto);

        mockMvc.perform(get("/events/1/participants")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"event_write"})
    public void testGetParticipantsAlphabetical() throws Exception {
        List<User> participants = getUsers();
        List<UserGetDto> dtos = new ArrayList<>();
        UserGetDto dto1 = new UserGetDto();
        UserGetDto dto2 = new UserGetDto();
        dto1.setId(1L);
        dto1.setFirstName("FirstnameA");
        dto1.setLastName("LastnameA");
        dto2.setId(2L);
        dto2.setFirstName("FirstnameB");
        dto2.setLastName("LastnameB");
        dtos.add(dto1);
        dtos.add(dto2);

        when(eventService.isEventOwner(eq(1L), any(User.class))).thenReturn(true);
        when(userService.findUser("1")).thenReturn(testUser);
        when(eventService.getParticipantsAlphabetical(eq(1L), any(User.class))).thenReturn(participants);
        when(modelMapper.map(any(User.class), eq(UserGetDto.class))).thenReturn(dto1, dto2);
        mockMvc.perform(get("/events/1/participants/alphabetical")).andExpect(status().isOk()).andExpect(jsonPath("$[0].firstName").value("FirstnameA")).andExpect(jsonPath("$[1].firstName").value("FirstnameB"));
    }

    private static List<User> getUsers() throws NoSuchFieldException, IllegalAccessException {
        List<User> participants = new ArrayList<>();
        User user1 = new User();
        User user2 = new User();
        java.lang.reflect.Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user1, 1L);
        idField.set(user2, 2L);
        user1.setFirstName("FirstnameA");
        user1.setLastName("LastnameA");
        user2.setFirstName("FirstnameB");
        user2.setLastName("LastnameB");
        participants.add(user1);
        participants.add(user2);
        return participants;
    }

    @Test
    public void testGetParticipantCount() throws Exception {
        when(eventService.getTotalParticipantCount(eq(1L))).thenReturn(5);
        mockMvc.perform(get("/events/1/participants/count")).andExpect(status().isOk()).andExpect(content().string("5"));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"event_read"})
    public void testConfirmAttendanceDuplicate() throws Exception {
        when(userService.findUser("1")).thenReturn(testUser);
        EventParticipation existingParticipation = new EventParticipation();
        existingParticipation.setAttend(true);
        when(participationRepository.findByEventIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingParticipation));
        mockMvc.perform(post("/events/1/attend")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"event_read"})
    public void testConfirmAttendanceEventNotFound() throws Exception {
        when(userService.findUser("1")).thenReturn(testUser);
        when(participationRepository.findByEventIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(eventService.confirmAttendance(9999L, testUser)).thenThrow(new EntityNotFoundException("event", "9999"));
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }
}