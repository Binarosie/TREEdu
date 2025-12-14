package vn.hcmute.edu.materialsservice.Dto;

import vn.hcmute.edu.materialsservice.Model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private String id;
    private String email;
    private String name;
    private String role;
    private String status;

    public UserInfoDTO mapToUserInfo(User user) {
        this.id = user.getUserId().toString();
        this.email = user.getEmail();
        this.name = user.getFullName();
        this.role = user.getClass().getSimpleName();
        this.status = user.isActive() ? "Active" : "Inactive";

        return this;
    }
}