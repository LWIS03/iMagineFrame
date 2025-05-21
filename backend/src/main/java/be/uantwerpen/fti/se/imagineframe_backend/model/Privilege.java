package be.uantwerpen.fti.se.imagineframe_backend.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "imf_privileges")
public class Privilege {
    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column
    private String name;
    @Column
    private String description;

    public Privilege(String name) {
        this.name = name;
        this.description = "";
    }

    public Privilege(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
