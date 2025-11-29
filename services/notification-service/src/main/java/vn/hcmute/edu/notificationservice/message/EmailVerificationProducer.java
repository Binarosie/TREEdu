package vn.hcmute.edu.notificationservice.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.notificationservice.dto.kafka.EmailVerificationEvent;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.email-verification:email-verification}")
    private String emailVerificationTopic;

    /**
     * Publish email verification event to Kafka for auth-service
     * @param event Email verification event
     */
    public void publishEmailVerificationEvent(EmailVerificationEvent event) {
        log.info("Publishing email verification event for user: {} with status: {}", 
                event.getVerificationData().getUserId(), event.getEventType());

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                    emailVerificationTopic,
                    event.getVerificationData().getUserId(), // Use userId as key for partitioning
                    event
            );

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to send email verification event for user: {}. Error: {}",
                            event.getVerificationData().getUserId(), throwable.getMessage(), throwable);
                } else {
                    log.info("Successfully sent email verification event for user: {} to topic: {} at offset: {}",
                            event.getVerificationData().getUserId(),
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception ex) {
            log.error("Error publishing email verification event for user: {}. Error: {}",
                    event.getVerificationData().getUserId(), ex.getMessage(), ex);
            throw new RuntimeException("Failed to publish email verification event", ex);
        }
    }
}