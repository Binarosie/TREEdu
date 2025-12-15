package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Enum.LearningStatus;
import vn.hcmute.edu.materialsservice.Model.FlashcardProgress;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends MongoRepository<FlashcardProgress, String> {

    Optional<FlashcardProgress> findByUserIdAndFlashcardId(String userId, String flashcardId);

    List<FlashcardProgress> findByUserId(String userId);

    List<FlashcardProgress> findByUserIdAndStatus(String userId, LearningStatus status);

    List<FlashcardProgress> findByFlashcardId(String flashcardId);

    boolean existsByUserIdAndFlashcardId(String userId, String flashcardId);

    void deleteByFlashcardId(String flashcardId);
}
