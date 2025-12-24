package vn.hcmute.edu.materialsservice.dtos.request.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    // Field để admin thay đổi role của user khác
    private String role; // "MEMBER", "SUPPORTER", "ADMIN"}
}