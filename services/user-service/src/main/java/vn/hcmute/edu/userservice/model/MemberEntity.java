package vn.hcmute.edu.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.hcmute.edu.userservice.enums.ESkillLevel;

import java.time.Instant;

@Entity
@Table(name = "members")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@PrimaryKeyJoinColumn(name = "user_id")
public class MemberEntity extends UserEntity {

    @NotNull(message = "Skill level must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false)
    private ESkillLevel level;


    @PastOrPresent(message = "Added date must be in the past or present")
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt = Instant.now();
}