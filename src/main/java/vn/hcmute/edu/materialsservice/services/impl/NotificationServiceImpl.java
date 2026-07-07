package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.dtos.response.NotificationDTO;
import vn.hcmute.edu.materialsservice.models.Notification;
import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import vn.hcmute.edu.materialsservice.repository.NotificationRepository;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.iNotificationService;
import vn.hcmute.edu.materialsservice.services.observer.NotificationCenter;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements iNotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationDTO> getMyNotifications(String receiverId) {
        return notificationRepository
                .findByReceiverIdAndDeletedFalseOrderByCreatedAtDesc(receiverId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public long countUnread(String receiverId) {
        return notificationRepository.countByReceiverIdAndIsSeenFalseAndDeletedFalse(receiverId);
    }

    @Override
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsSeen(true);
            n.setSeenAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsRead(String receiverId) {
        List<Notification> unread = notificationRepository
                .findByReceiverIdAndDeletedFalseOrderByCreatedAtDesc(receiverId)
                .stream()
                .filter(n -> Boolean.FALSE.equals(n.getIsSeen()))
                .toList();

        unread.forEach(n -> {
            n.setIsSeen(true);
            n.setSeenAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unread);
    }

    @Override
    public void sendToUser(String receiverId, String title, String content) {
        NotificationCenter.notifyObservers(NotificationEvent.builder()
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .type("ADMIN_MESSAGE")
                .build());
    }

    @Override
    public void sendAppealToAdmins(String fromUserId, String fromUserEmail, String content) {
        List<String> adminIds = userRepository
                .findByUserType("vn.hcmute.edu.materialsservice.models.Admin")
                .stream()
                .map(vn.hcmute.edu.materialsservice.models.User::getId)
                .toList();

        if (adminIds.isEmpty()) {
            log.warn("[NOTI] APPEAL | SKIPPED - no admins found");
            return;
        }

        NotificationCenter.notifyObservers(NotificationEvent.builder()
                .receiverIds(adminIds)
                .title("Kháng cáo từ người dùng")
                .content("User " + fromUserEmail + " (" + fromUserId + ") gửi phản hồi: " + content)
                .type("APPEAL")
                .build());
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .receiverId(n.getReceiverId())
                .title(n.getTitle())
                .content(n.getContent())
                .type(n.getType())
                .isSeen(n.getIsSeen())
                .createdAt(n.getCreatedAt())
                .seenAt(n.getSeenAt())
                .build();
    }
}