package vn.hcmute.edu.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.notificationservice.config.NotificationProperties;
import vn.hcmute.edu.notificationservice.dto.EmailVerificationRequest;
import vn.hcmute.edu.notificationservice.dto.kafka.EmailVerificationEvent;
import vn.hcmute.edu.authservice.dto.kafka.UserRegistrationEvent;
import vn.hcmute.edu.notificationservice.enums.ENotificationStatus;
import vn.hcmute.edu.notificationservice.enums.ENotificationType;
import vn.hcmute.edu.notificationservice.model.EmailVerificationToken;
import vn.hcmute.edu.notificationservice.model.Notification;
import vn.hcmute.edu.notificationservice.model.NotificationHistory;
import vn.hcmute.edu.notificationservice.repository.EmailVerificationTokenRepository;
import vn.hcmute.edu.notificationservice.repository.NotificationHistoryRepository;
import vn.hcmute.edu.notificationservice.repository.NotificationRepository;
import vn.hcmute.edu.notificationservice.message.EmailVerificationProducer;
import vn.hcmute.edu.notificationservice.service.IEmailService;
import vn.hcmute.edu.notificationservice.service.INotificationService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationHistoryRepository notificationHistoryRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final IEmailService emailService;
    private final EmailVerificationProducer emailVerificationProducer;
    private final NotificationProperties properties;

    @Override
    public void processUserRegistration(UserRegistrationEvent event) {
        try {
            var userData = event.getUserData();
            log.info("Processing user registration for email: {}", userData.getEmail());

            // Generate verification token
            String token = generateVerificationToken();
            String verificationLink = buildVerificationLink(token, userData.getEmail());

            // Save verification token
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .token(token)
                    .userId(userData.getUserId())
                    .email(userData.getEmail())
                    .verified(false)
                    .expiresAt(Instant.now().plusSeconds(24 * 60 * 60)) // 24 hours
                    .build();

            tokenRepository.save(verificationToken);

            // Send verification email
            emailService.sendVerificationEmail(
                    userData.getEmail(),
                    userData.getFullName(),
                    verificationLink);

            // Save notification history
            NotificationHistory history = NotificationHistory.builder()
                    .userId(userData.getUserId())
                    .email(userData.getEmail())
                    .notificationType(ENotificationType.EMAIL_VERIFICATION)
                    .status(ENotificationStatus.SENT)
                    .content("Email verification sent to " + userData.getEmail())
                    .templateName("email-verification")
                    .verificationToken(token)
                    .sentAt(Instant.now())
                    .build();

            notificationHistoryRepository.save(history);

            log.info("Email verification sent successfully to: {}", userData.getEmail());

        } catch (Exception ex) {
            log.error("Failed to process user registration for email: {}. Error: {}",
                    event.getUserData().getEmail(), ex.getMessage(), ex);

            // Save failed notification history
            saveFailedNotificationHistory(event.getUserData(), ex.getMessage());
            throw new RuntimeException("Failed to process user registration", ex);
        }
    }

    @Override
    public boolean verifyEmail(EmailVerificationRequest request) {
        try {
            Optional<EmailVerificationToken> tokenOpt = tokenRepository.findByToken(request.getToken());

            if (tokenOpt.isEmpty()) {
                log.warn("Verification token not found: {}", request.getToken());
                return false;
            }

            EmailVerificationToken token = tokenOpt.get();

            if (token.isExpired()) {
                log.warn("Verification token expired: {}", request.getToken());
                return false;
            }

            if (token.isVerified()) {
                log.warn("Verification token already used: {}", request.getToken());
                return false;
            }

            if (!token.getEmail().equals(request.getEmail())) {
                log.warn("Email mismatch for token: {} - expected: {}, actual: {}",
                        request.getToken(), token.getEmail(), request.getEmail());
                return false;
            }

            // Mark token as verified
            token.setVerified(true);
            token.setVerifiedAt(Instant.now());
            tokenRepository.save(token);

            // Send email verification event to auth-service
            publishEmailVerificationEvent(token, true);

            // Send welcome email
            emailService.sendWelcomeEmail(token.getEmail(), "User"); // You may want to store fullName in token

            // Update notification history
            updateNotificationHistoryStatus(token.getToken(), ENotificationStatus.DELIVERED);

            log.info("Email verified successfully for: {}", request.getEmail());
            return true;

        } catch (Exception ex) {
            log.error("Failed to verify email: {}. Error: {}", request.getEmail(), ex.getMessage(), ex);
            return false;
        }
    }

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Override
    public void markAsRead(String notificationId, String userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            if (notification.getUserId().equals(userId)) {
                notification.setIsRead(true);
                notification.setStatus(ENotificationStatus.READ);
                notificationRepository.save(notification);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationHistory> getNotificationHistory(String userId) {
        return notificationHistoryRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildVerificationLink(String token, String email) {
        return properties.getEmail().getBaseUrl() + "/api/notifications/verify-email?token=" + token + "&email="
                + email;
    }

    private void publishEmailVerificationEvent(EmailVerificationToken token, boolean verified) {
        try {
            EmailVerificationEvent event = EmailVerificationEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType(verified ? "EMAIL_VERIFIED" : "EMAIL_VERIFICATION_FAILED")
                    .timestamp(Instant.now())
                    .verificationData(EmailVerificationEvent.VerificationData.builder()
                            .userId(token.getUserId())
                            .email(token.getEmail())
                            .verified(verified)
                            .verificationToken(token.getToken())
                            .verifiedAt(token.getVerifiedAt())
                            .build())
                    .build();

            emailVerificationProducer.publishEmailVerificationEvent(event);
            log.info("Published email verification event for user: {}", token.getUserId());

        } catch (Exception ex) {
            log.error("Failed to publish email verification event for user: {}. Error: {}",
                    token.getUserId(), ex.getMessage(), ex);
        }
    }

    private void saveFailedNotificationHistory(UserRegistrationEvent.UserData userData, String errorMessage) {
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .userId(userData.getUserId())
                    .email(userData.getEmail())
                    .notificationType(ENotificationType.EMAIL_VERIFICATION)
                    .status(ENotificationStatus.FAILED)
                    .content("Failed to send email verification to " + userData.getEmail())
                    .errorMessage(errorMessage)
                    .templateName("email-verification")
                    .build();

            notificationHistoryRepository.save(history);
        } catch (Exception ex) {
            log.error("Failed to save failed notification history: {}", ex.getMessage(), ex);
        }
    }

    private void updateNotificationHistoryStatus(String verificationToken, ENotificationStatus status) {
        try {
            Optional<NotificationHistory> historyOpt = notificationHistoryRepository
                    .findByVerificationToken(verificationToken);
            if (historyOpt.isPresent()) {
                NotificationHistory history = historyOpt.get();
                history.setStatus(status);
                notificationHistoryRepository.save(history);
            }
        } catch (Exception ex) {
            log.error("Failed to update notification history status: {}", ex.getMessage(), ex);
        }
    }
}