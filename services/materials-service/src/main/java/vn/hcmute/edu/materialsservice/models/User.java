package vn.hcmute.edu.materialsservice.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements Serializable {
    @Id
    private UUID id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;
    private boolean isActive;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            id = UUID.randomUUID();
        }
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();

        }
        this.modifiedOn = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.modifiedOn = LocalDateTime.now();
    }
}