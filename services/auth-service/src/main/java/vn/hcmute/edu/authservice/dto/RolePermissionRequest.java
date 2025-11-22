package vn.hcmute.edu.authservice.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.authservice.constants.Messages;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionRequest {
    @NotBlank(message = Messages.Validation.PERMISSION_CODE_REQUIRED)
    private String permission;
}
