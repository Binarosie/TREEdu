package vn.hcmute.edu.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.notificationservice.enums.ENotificationType;
import vn.hcmute.edu.notificationservice.enums.ENotificationStatus;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_history")
public class NotificationHistory extends AbstractAuditingEntity<String> {

    @Id
    private String id;

    private String userId;
    private String email;
    private ENotificationType notificationType;
    private ENotificationStatus status;
    private String content;
    private String errorMessage;

    @lombok.Builder.Default
    private int retryCount = 0;

    private Instant sentAt;

    // Additional metadata
    private String templateName;
    private String verificationToken;

    @Override
    public String getId() {
        return this.id;
    }
}