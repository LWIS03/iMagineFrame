package be.uantwerpen.fti.se.imagineframe_backend.model;

import be.uantwerpen.fti.se.imagineframe_backend.label.ProjectStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "imf_projects")
public class Project {
    private static final String DEFAULT_NAME = "default";
    private static final String DEFAULT_DESCRIPTION = "default";
    private static final String DEFAULT_MEDIA = "";
    private static final ProjectStatus DEFAULT_STATUS = ProjectStatus.PLANNING;
    private static final boolean DEFAULT_IS_PUBLIC = true;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(length = 255)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "imf_project_mtm_user")
    private Set<User> users = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private boolean isPublic = true;

    public Project() {
        this.name = DEFAULT_NAME;
        this.description = DEFAULT_DESCRIPTION;
        this.mediaUrl = DEFAULT_MEDIA;
        this.status = DEFAULT_STATUS;
        this.users = new HashSet<>();
        this.owner = null;
        this.isPublic = DEFAULT_IS_PUBLIC;
    }

    public Project(String name, String description, String mediaUrl, ProjectStatus status, User owner) {
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.status = status;
        this.users = new HashSet<>();
        this.owner = owner;
        this.isPublic = DEFAULT_IS_PUBLIC;
    }

    public Project(String name, String description, String mediaUrl, ProjectStatus status, User owner, boolean isPublic) {
        this.name = name;
        this.description = description;
        this.mediaUrl = mediaUrl;
        this.status = status;
        this.users = new HashSet<>();
        this.owner = owner;
        this.isPublic = isPublic;
    }
}
