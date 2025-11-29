package vn.hcmute.edu.notificationservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.notificationservice.enums.ENotificationType;
import vn.hcmute.edu.notificationservice.enums.ENotificationStatus;
import vn.hcmute.edu.notificationservice.model.Notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByUserIdOrderByCreatedDateDesc(String userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedDateDesc(String userId);

    List<Notification> findByUserIdAndTypeOrderByCreatedDateDesc(String userId, ENotificationType type);

    List<Notification> findByUserIdAndStatusOrderByCreatedDateDesc(String userId, ENotificationStatus status);

    long countByUserIdAndIsReadFalse(String userId);

    List<Notification> findByCreatedDateBetween(Instant startDate, Instant endDate);

    void deleteByUserIdAndCreatedDateBefore(String userId, Instant before);

    boolean existsByNotificationId(UUID notificationId);
}