package vn.hcmute.edu.materialsservice.services;

import vn.hcmute.edu.materialsservice.dtos.response.NotificationDTO;
import java.util.List;

public interface iNotificationService {
    List<NotificationDTO> getMyNotifications(String receiverId);
    long countUnread(String receiverId);
    void markAsRead(String notificationId);
    void markAllAsRead(String receiverId);
    void sendToUser(String receiverId, String title, String content);
    void sendAppealToAdmins(String fromUserId, String fromUserEmail, String content);
}