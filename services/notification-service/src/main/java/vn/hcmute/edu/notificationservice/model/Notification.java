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

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification extends AbstractAuditingEntity<String> {

    @Id
    private String id;

    @lombok.Builder.Default
    private UUID notificationId = UUID.randomUUID();

    private String content;

    @lombok.Builder.Default
    private Boolean isRead = false;

    private ENotificationType type;

    // Reference to user
    private String userId;

    // Additional fields for tracking
    private ENotificationStatus status;

    @Override
    public String getId() {
        return this.id;
    }
}