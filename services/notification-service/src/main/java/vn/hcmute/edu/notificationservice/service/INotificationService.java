package vn.hcmute.edu.notificationservice.service;

import vn.hcmute.edu.notificationservice.dto.EmailVerificationRequest;
import vn.hcmute.edu.authservice.dto.kafka.UserRegistrationEvent;
import vn.hcmute.edu.notificationservice.model.Notification;
import vn.hcmute.edu.notificationservice.model.NotificationHistory;

import java.util.List;

public interface INotificationService {

    /**
     * Process user registration event and send verification email
     * 
     * @param event User registration event from Kafka
     */
    void processUserRegistration(UserRegistrationEvent event);

    /**
     * Verify email using verification token
     * 
     * @param request Email verification request
     * @return true if verification successful, false otherwise
     */
    boolean verifyEmail(EmailVerificationRequest request);

    /**
     * Create and save notification
     * 
     * @param notification Notification to save
     * @return Saved notification
     */
    Notification createNotification(Notification notification);

    /**
     * Get notifications for user
     * 
     * @param userId User ID
     * @return List of notifications
     */
    List<Notification> getNotificationsForUser(String userId);

    /**
     * Mark notification as read
     * 
     * @param notificationId Notification ID
     * @param userId         User ID
     */
    void markAsRead(String notificationId, String userId);

    /**
     * Get unread notification count for user
     * 
     * @param userId User ID
     * @return Count of unread notifications
     */
    long getUnreadCount(String userId);

    /**
     * Get notification history for user
     * 
     * @param userId User ID
     * @return List of notification history
     */
    List<NotificationHistory> getNotificationHistory(String userId);
}