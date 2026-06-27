package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;
import vn.hcmute.edu.materialsservice.models.FlashcardProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends MongoRepository<FlashcardProgress, String> {

    Optional<FlashcardProgress> findFirstByUserIdAndFlashcardId(String userId, String flashcardId);

    Optional<FlashcardProgress> findFirstByUserIdAndFlashcardIdOrderByStartedAtDesc(String userId, String flashcardId);

    List<FlashcardProgress> findByUserId(String userId);

    List<FlashcardProgress> findByUserIdAndStatus(String userId, ELearningStatus status);

    List<FlashcardProgress> findByFlashcardId(String flashcardId);

    int countByFlashcardId(String flashcardId);

    List<FlashcardProgress> findByUserIdAndFlashcardId(String userId, String flashcardId);
}
