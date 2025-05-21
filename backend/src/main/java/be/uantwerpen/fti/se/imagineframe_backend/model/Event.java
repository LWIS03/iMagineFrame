package be.uantwerpen.fti.se.imagineframe_backend.model;
import be.uantwerpen.fti.se.imagineframe_backend.label.EventLabel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "imf_events")
public class Event {
    private static final String DEFAULT_NAME = "default";
    private static final String DEFAULT_DESCRIPTION = "default";
    private static final String DEFAULT_LOCATION = "default";
    private static final LocalDateTime DEFAULT_STARTDATE = null;
    private static final LocalDateTime DEFAULT_ENDDATE = null;
    private static final String DEFAULT_IMAGE = "";
    private static final EventLabel DEFAULT_LABEL = EventLabel.OTHER;
    private static final User DEFAULT_USER = null;
    private static final boolean DEFAULT_IS_PUBLIC = true;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startdate;

    @Column(nullable = false)
    private LocalDateTime enddate;

    @Column(length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventLabel label = EventLabel.OTHER;

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private boolean isPublic = true;

    public Event() {
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.location = DEFAULT_LOCATION;
        this.startdate = DEFAULT_STARTDATE;
        this.enddate = DEFAULT_ENDDATE;
        this.imageUrl = DEFAULT_IMAGE;
        this.label = DEFAULT_LABEL;
        this.owner = DEFAULT_USER;
        this.isPublic = DEFAULT_IS_PUBLIC;
    }

    public Event(String name, String description, String location, LocalDateTime startdate, LocalDateTime enddate, String imageUrl, EventLabel label, User owner, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.startdate = startdate;
        this.enddate = enddate;
        this.imageUrl = imageUrl;
        this.label = label;
        this.owner = owner;
        this.isPublic = isPublic;
    }

    //this way the isPublic variable is set to 'true' at default
    public Event(String name, String description, String location, LocalDateTime startdate, LocalDateTime enddate, String imageUrl, EventLabel label, User owner) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.startdate = startdate;
        this.enddate = enddate;
        this.imageUrl = imageUrl;
        this.label = label;
        this.owner = owner;
        this.isPublic = DEFAULT_IS_PUBLIC;
    }
}