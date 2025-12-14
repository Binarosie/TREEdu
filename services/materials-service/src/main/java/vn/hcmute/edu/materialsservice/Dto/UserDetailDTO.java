package vn.hcmute.edu.materialsservice.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.materialsservice.Model.Supporter;
import vn.hcmute.edu.materialsservice.Model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailDTO {
    private String id;
    private String email;
    private String name;
    private String role;
    private String status;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    public static UserDetailDTO mapTo(User user) {
        UserDetailDTO dto = new UserDetailDTO();
        dto.setId(user.getUserId().toString());
        dto.setEmail(user.getEmail());
        dto.setName(user.getFullName());
        dto.setStatus(user.isActive() ? "Active" : "Inactive");
        dto.setCreatedOn(user.getCreatedOn());
        dto.setModifiedOn(user.getModifiedOn());

        if (user instanceof Supporter moderator) {
            dto.setRole("Supporter");
        } else {
            dto.setRole("Admin");
        }
        return dto;
    }
}