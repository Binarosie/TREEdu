package vn.hcmute.edu.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.notificationservice.enums.ENotificationType;
import vn.hcmute.edu.notificationservice.enums.ENotificationStatus;
import vn.hcmute.edu.notificationservice.model.NotificationHistory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationHistoryRepository extends MongoRepository<NotificationHistory, String> {

    List<NotificationHistory> findByUserIdOrderByCreatedDateDesc(String userId);

    List<NotificationHistory> findByEmailOrderByCreatedDateDesc(String email);

    List<NotificationHistory> findByNotificationTypeAndStatusOrderByCreatedDateDesc(
            ENotificationType notificationType, ENotificationStatus status);

    Optional<NotificationHistory> findByVerificationToken(String verificationToken);

    List<NotificationHistory> findByStatusAndCreatedDateBefore(ENotificationStatus status, Instant before);

    long countByUserIdAndNotificationTypeAndCreatedDateAfter(
            String userId, ENotificationType notificationType, Instant after);
}