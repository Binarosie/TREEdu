package vn.hcmute.edu.materialsservice.Dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EUserStatus;
import vn.hcmute.edu.materialsservice.Enum.ROLE;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserResponse {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private ROLE roles;
    private EUserStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
