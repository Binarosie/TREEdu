package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {

    List<Flashcard> findByLevel(Integer level);

    @Query("{'topic': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTopicContainingIgnoreCase(String topic);

    @Query("{'topic': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTopicFuzzy(String regexPattern);

    @Query("{'level': ?0, 'topic': {$regex: ?1, $options: 'i'}}")
    List<Flashcard> findByLevelAndTopic(Integer level, String topic);

    @Query(value = "{'title': {$regex: ?0, $options: 'i'}}", count = true)
    long countByTitleIgnoreCase(String title);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTitleContainingIgnoreCase(String title);

    List<Flashcard> findByVisibility(EFlashcardVisibility visibility);

    @Query("{'visibility': ?0, 'topic': {$regex: ?1, $options: 'i'}}")
    List<Flashcard> findByVisibilityAndTopic(EFlashcardVisibility visibility, String topic);

    List<Flashcard> findByVisibilityAndLevel(EFlashcardVisibility visibility, Integer level);

    List<Flashcard> findByCreatedBy(String createdBy);

    @Query("{'visibility': ?0, 'isViolated': false}")
    List<Flashcard> findPublicNotViolated(EFlashcardVisibility visibility);
}
