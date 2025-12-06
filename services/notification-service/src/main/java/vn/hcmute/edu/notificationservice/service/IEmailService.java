package vn.hcmute.edu.notificationservice.service;

import java.util.Map;

public interface IEmailService {

    /**
     * Send verification email to user
     * 
     * @param email            User's email address
     * @param fullName         User's full name
     * @param verificationLink Verification link
     */
    void sendVerificationEmail(String email, String fullName, String verificationLink);

    /**
     * Send welcome email to newly registered user
     * 
     * @param email    User's email address
     * @param fullName User's full name
     */
    void sendWelcomeEmail(String email, String fullName);

    /**
     * Send email using template
     * 
     * @param to           Recipient email
     * @param subject      Email subject
     * @param templateName Template name
     * @param variables    Template variables
     */
    void sendEmailWithTemplate(String to, String subject, String templateName, Map<String, Object> variables);
}