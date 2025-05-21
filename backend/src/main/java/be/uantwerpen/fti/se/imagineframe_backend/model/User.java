package be.uantwerpen.fti.se.imagineframe_backend.model;

import be.uantwerpen.fti.se.imagineframe_backend.label.PrivacyLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "imf_users")
public class User {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true)
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "imf_user_mtm_group",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "group_id", referencedColumnName = "id", nullable = false)}
    )
    private Set<Group> groups;

    @Setter(AccessLevel.NONE)
    @Column(name = "date_created", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_on")
    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    public User() {
        this.groups = new HashSet<>();
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.groups = new HashSet<>();
    }

    public User(String email,
                String password,
                String firstName,
                String lastName,
                String username,
                Set<Group> groups,
                LocalDateTime dateCreated,
                LocalDateTime updateDateTime) {
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.groups = groups;
        this.dateCreated = dateCreated;
        this.updateDateTime = updateDateTime;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
