package vn.hcmute.edu.notificationservice.message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.notificationservice.dto.kafka.UserRegistrationEvent;
import vn.hcmute.edu.notificationservice.service.INotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationConsumer {

    private final INotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.topics.user-registration:user-registration}", groupId = "${spring.kafka.consumer.group-id:notification-service-group}", containerFactory = "kafkaListenerContainerFactory")
    public void handleUserRegistration(
            @Payload UserRegistrationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received user registration event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);
        log.debug("Event details: {}", event);

        try {
            // Validate event
            if (event == null || event.getUserData() == null) {
                log.error("Invalid user registration event: {}", event);
                acknowledgment.acknowledge();
                return;
            }

            if (!"USER_REGISTERED".equals(event.getEventType())) {
                log.warn("Unknown event type: {}, skipping", event.getEventType());
                acknowledgment.acknowledge();
                return;
            }

            // Process the registration - send verification email
            var userData = event.getUserData();
            log.info("Processing user registration notification for email: {}", userData.getEmail());

            // Process user registration and send verification email
            notificationService.processUserRegistration(event);

            log.info("Successfully processed user registration notification for email: {}", userData.getEmail());

            // Acknowledge successful processing
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Error processing user registration event: {}", ex.getMessage(), ex);

            // Don't acknowledge - let retry mechanism handle it
            throw new RuntimeException("Failed to process user registration event", ex);
        }
    }
}