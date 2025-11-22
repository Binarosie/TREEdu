package vn.hcmute.edu.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.authservice.constants.Messages;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = Messages.Validation.USERNAME_REQUIRED)
    private String usernameOrEmail;
    @NotBlank(message = Messages.Validation.PASSWORD_REQUIRED)
    @Size(min = 8, max = 64, message = Messages.Validation.PASSWORD_LENGTH)
    private String password;
}