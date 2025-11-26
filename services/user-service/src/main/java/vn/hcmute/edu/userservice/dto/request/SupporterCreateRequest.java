package vn.hcmute.edu.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import vn.hcmute.edu.userservice.enums.EUserRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupporterCreateRequest {

    // ===== USER FIELDS =====
    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 255, message = "Full name must be between 3–255 characters")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+84)?\\d{10}$", message = "Phone number must be 10 digits or start with +84")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private EUserRole userRole = EUserRole.SUPPORTER;

    // ===== HR FIELDS =====

}