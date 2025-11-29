package vn.hcmute.edu.notificationservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.email")
@Data
public class EmailProperties {
    private String from;
    private Templates templates = new Templates();

    @Data
    public static class Templates {
        private String userRegistration = "user-registration-welcome";
        private String emailVerification = "email-verification";
    }
}