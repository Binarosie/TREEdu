package vn.hcmute.edu.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "supporter_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@PrimaryKeyJoinColumn(name = "user_id")
public class SupporterEntity extends UserEntity {
    /**
     * Người duyệt (admin hoặc hệ thống), có thể null nếu chưa duyệt.
     */
    @Column(name = "approved_by")
    private String approvedBy;

    @PastOrPresent(message = "Approval date must be in the past or present")
    @Column(name = "approved_at")
    private Instant approvedAt;

    @Override
    public UUID getId() {
        return super.getId();
    }
}