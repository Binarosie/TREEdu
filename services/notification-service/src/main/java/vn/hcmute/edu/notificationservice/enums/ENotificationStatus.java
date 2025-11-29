package vn.hcmute.edu.notificationservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ENotificationStatus {
    PENDING("Pending"),
    SENT("Sent"),
    FAILED("Failed"),
    DELIVERED("Delivered"),
    READ("Read");

    private final String displayName;
}