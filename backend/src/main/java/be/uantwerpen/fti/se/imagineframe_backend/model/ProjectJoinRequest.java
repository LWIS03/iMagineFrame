package be.uantwerpen.fti.se.imagineframe_backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imf_project_join_requests")
public class ProjectJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Setter(AccessLevel.NONE)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column
    private Boolean accepted;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Setter(AccessLevel.NONE)
    @UpdateTimestamp
    private LocalDateTime dateUpdated;

    public ProjectJoinRequest(User user, Project project) {
        this.user = user;
        this.project = project;
    }
}