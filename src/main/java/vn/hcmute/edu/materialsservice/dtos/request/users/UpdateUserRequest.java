package vn.hcmute.edu.materialsservice.dtos.request.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private String role; // "MEMBER", "SUPPORTER"}
    private Boolean canPublishFlashcard;
    private Boolean canReportFlashcard;
}