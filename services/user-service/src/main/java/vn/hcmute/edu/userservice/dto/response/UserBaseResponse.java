package vn.hcmute.edu.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import vn.hcmute.edu.userservice.enums.EUserRole;
import vn.hcmute.edu.userservice.enums.EUserStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserBaseResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phoneNumber;

    private EUserRole userRole;
    private EUserStatus userStatus;
    private Instant lastLogin;

    // auditing info
    private String createdBy;
    private Instant createdDate;
    private String lastModifiedBy;
    private Instant lastModifiedDate;
    private boolean isDeleted;
}