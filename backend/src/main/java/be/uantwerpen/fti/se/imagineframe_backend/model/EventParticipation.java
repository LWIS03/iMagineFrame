package be.uantwerpen.fti.se.imagineframe_backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "imf_event_participation", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
public class EventParticipation {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean attend;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    private LocalDateTime created;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime updated;
}
