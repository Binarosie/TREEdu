package vn.hcmute.edu.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Data
public class NotificationProperties {
    private Kafka kafka = new Kafka();
    private Email email = new Email();
    private Notification notification = new Notification();

    @Data
    public static class Kafka {
        private Topics topics = new Topics();

        @Data
        public static class Topics {
            private String userRegistration = "user-registration";
            private String emailVerification = "email-verification";
        }
    }

    @Data
    public static class Email {
        private String from;
        private String baseUrl = "http://localhost:8084";
        private Templates templates = new Templates();

        @Data
        public static class Templates {
            private String userRegistration = "user-registration-welcome";
            private String emailVerification = "email-verification";
        }
    }

    @Data
    public static class Notification {
        private Retry retry = new Retry();

        @Data
        public static class Retry {
            private int maxAttempts = 3;
            private long delay = 1000;
        }
    }
}