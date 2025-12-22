package vn.hcmute.edu.materialsservice.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.hcmute.edu.materialsservice.models.Supporter;
import vn.hcmute.edu.materialsservice.models.User;

import java.time.LocalDateTime;

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
        dto.setId(user.getId().toString());
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