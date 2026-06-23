package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.dtos.response.NotificationDTO;
import vn.hcmute.edu.materialsservice.models.Notification;
import vn.hcmute.edu.materialsservice.repository.NotificationRepository;
import vn.hcmute.edu.materialsservice.services.iNotificationService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements iNotificationService {

    private final NotificationRepository notificationRepository;

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