package vn.hcmute.edu.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.notificationservice.dto.EmailVerificationRequest;
import vn.hcmute.edu.notificationservice.model.Notification;
import vn.hcmute.edu.notificationservice.model.NotificationHistory;
import vn.hcmute.edu.notificationservice.service.INotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final INotificationService notificationService;

    /**
     * Verify email using verification token
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            boolean verified = notificationService.verifyEmail(request);
            if (verified) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email verification failed. Invalid or expired token."));
            }
        } catch (Exception ex) {
            log.error("Error verifying email: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error"));
        }
    }

    /**
     * Verify email via GET request (for email link clicks)
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmailViaLink(
            @RequestParam String token,
            @RequestParam String email) {
        try {
            EmailVerificationRequest request = EmailVerificationRequest.builder()
                    .token(token)
                    .email(email)
                    .build();

            boolean verified = notificationService.verifyEmail(request);
            if (verified) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Email verified successfully! You can now login to your account."));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email verification failed. Invalid or expired token."));
            }
        } catch (Exception ex) {
            log.error("Error verifying email via link: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error"));
        }
    }

    /**
     * Get notifications for user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsForUser(@PathVariable String userId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception ex) {
            log.error("Error getting notifications for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable String notificationId,
            @RequestParam String userId) {
        try {
            notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notification marked as read"));
        } catch (Exception ex) {
            log.error("Error marking notification as read: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Internal server error"));
        }
    }

    /**
     * Get unread notification count for user
     */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception ex) {
            log.error("Error getting unread count for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get notification history for user
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<List<NotificationHistory>> getNotificationHistory(@PathVariable String userId) {
        try {
            List<NotificationHistory> history = notificationService.getNotificationHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception ex) {
            log.error("Error getting notification history for user {}: {}", userId, ex.getMessage(), ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "notification-service"));
    }
}