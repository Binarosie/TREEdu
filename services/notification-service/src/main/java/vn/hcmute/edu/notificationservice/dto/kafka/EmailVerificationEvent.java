package vn.hcmute.edu.notificationservice.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationEvent {

    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("eventType")
    private String eventType; // "EMAIL_VERIFIED" or "EMAIL_VERIFICATION_FAILED"

    @JsonProperty("timestamp")
    private Instant timestamp;

    @JsonProperty("verificationData")
    private VerificationData verificationData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationData {
        @JsonProperty("userId")
        private String userId;

        @JsonProperty("email")
        private String email;

        @JsonProperty("verified")
        private boolean verified;

        @JsonProperty("verificationToken")
        private String verificationToken;

        @JsonProperty("verifiedAt")
        private Instant verifiedAt;
    }
}