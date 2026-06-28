package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hcmute.edu.materialsservice.models.Notification;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByReceiverIdAndDeletedFalseOrderByCreatedAtDesc(String receiverId);
    long countByReceiverIdAndIsSeenFalseAndDeletedFalse(String receiverId);
}