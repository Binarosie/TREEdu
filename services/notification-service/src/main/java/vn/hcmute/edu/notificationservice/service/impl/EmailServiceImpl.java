package vn.hcmute.edu.notificationservice.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.hcmute.edu.notificationservice.config.NotificationProperties;
import vn.hcmute.edu.notificationservice.service.IEmailService;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final NotificationProperties properties;

    @Override
    public void sendVerificationEmail(String email, String fullName, String verificationLink) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("supportEmail", "support@treedu.edu.vn");

            String htmlContent = templateEngine.process("email-verification", context);

            sendHtmlEmail(
                    email,
                    "Xác thực email của bạn - TREEdu",
                    htmlContent
            );

            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send verification email to: {}. Error: {}", email, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send verification email", ex);
        }
    }

    @Override
    public void sendWelcomeEmail(String email, String fullName) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("loginUrl", properties.getEmail().getBaseUrl() + "/login");
            context.setVariable("supportEmail", "support@treedu.edu.vn");

            String htmlContent = templateEngine.process("user-registration-welcome", context);

            sendHtmlEmail(
                    email,
                    "Chào mừng bạn đến với TREEdu!",
                    htmlContent
            );

            log.info("Welcome email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send welcome email to: {}. Error: {}", email, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send welcome email", ex);
        }
    }

    @Override
    public void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            Context context = new Context();
            variables.forEach(context::setVariable);

            String htmlContent = templateEngine.process(templateName, context);
            sendHtmlEmail(to, subject, htmlContent);

            log.info("Email sent successfully to: {} using template: {}", to, templateName);

        } catch (Exception ex) {
            log.error("Failed to send email to: {} using template: {}. Error: {}", 
                    to, templateName, ex.getMessage(), ex);
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(properties.getEmail().getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}