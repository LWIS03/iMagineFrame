package be.uantwerpen.fti.se.imagineframe_backend.model;

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
@Table(name = "imf_groups")
public class Group {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "imf_group_mtm_privilege",
            joinColumns = {@JoinColumn(name = "GROUP_ID", referencedColumnName = "ID")},
            inverseJoinColumns = {@JoinColumn(name = "PRIV_ID", referencedColumnName = "ID")})
    private Set<Privilege> privileges;

    @ManyToMany(mappedBy = "groups")
    @JsonIgnore
    private Set<User> users;

    @Setter(AccessLevel.NONE)
    @Column(name = "date_created", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateCreated;

    @Setter(AccessLevel.NONE)
    @Column(name = "updated_on")
    @UpdateTimestamp
    private LocalDateTime updateDateTime;

    public Group() {
        this.privileges = new HashSet<>();
        this.users = new HashSet<>();
    }

    public Group(String name) {
        this.name = name;
    }
}
