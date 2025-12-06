package vn.hcmute.edu.notificationservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "email_verification_tokens")
public class EmailVerificationToken extends AbstractAuditingEntity<String> {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @Indexed
    private String userId;

    @Indexed
    private String email;

    private boolean verified;
    private Instant expiresAt;
    private Instant verifiedAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    @Override
    public String getId() {
        return this.id;
    }
}