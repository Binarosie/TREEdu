package vn.hcmute.edu.userservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import vn.hcmute.edu.userservice.enums.ESkillLevel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateRequest {

    // ===== USER FIELDS =====
    @Size(min = 3, max = 255, message = "Full name must be between 3–255 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^(\\+84)?\\d{10}$", message = "Invalid phone number format")
    private String phoneNumber;

    // ===== MEMBER FIELDS =====
    @NotNull(message = "Skill level is required")
    private ESkillLevel level;
}