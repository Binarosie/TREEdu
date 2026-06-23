package vn.hcmute.edu.materialsservice.services.observer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.models.Notification;
import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import vn.hcmute.edu.materialsservice.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WordNotificationObserver implements NotificationObserver {

    private final NotificationRepository notificationRepository;

    @PostConstruct
    public void init() {
        NotificationCenter.registerObserver(this);
    }

    @Override
    public void onNotify(NotificationEvent event) {
        List<String> targets = resolveReceivers(event);

        List<Notification> notifications = targets.stream()
                .map(receiverId -> Notification.builder()
                        .receiverId(receiverId)
                        .title(event.getTitle())
                        .content(event.getContent())
                        .type(event.getType())
                        .isSeen(false)
                        .createdAt(LocalDateTime.now())
                        .deleted(false)
                        .build())
                .toList();

        notificationRepository.saveAll(notifications);
        log.info("Saved {} notification(s): [{}] {}", notifications.size(), event.getType(), event.getTitle());
    }

    private List<String> resolveReceivers(NotificationEvent event) {
        List<String> targets = new ArrayList<>();
        if (event.getReceiverIds() != null && !event.getReceiverIds().isEmpty()) {
            targets.addAll(event.getReceiverIds());
        }
        if (event.getReceiverId() != null) {
            targets.add(event.getReceiverId());
        }
        return targets;
    }
}