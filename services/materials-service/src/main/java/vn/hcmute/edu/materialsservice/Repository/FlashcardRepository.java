package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Model.Flashcard;

import java.util.List;

@Repository
public interface FlashcardRepository extends MongoRepository<Flashcard, String> {

    /**
     * Tìm flashcard theo level
     */
    List<Flashcard> findByLevel(Integer level);

    /**
     * Tìm flashcard theo topic (case-insensitive)
     */
    @Query("{'topic': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTopicContainingIgnoreCase(String topic);

    /**
     * Fuzzy search: Tìm flashcard theo topic với regex pattern
     */
    @Query("{'topic': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTopicFuzzy(String regexPattern);

    /**
     * Tìm flashcard theo cả level và topic
     */
    @Query("{'level': ?0, 'topic': {$regex: ?1, $options: 'i'}}")
    List<Flashcard> findByLevelAndTopic(Integer level, String topic);

    /**
     * Kiểm tra xem title đã tồn tại chưa (case-insensitive)
     * SỬA: Đổi từ boolean sang Boolean và dùng countBy thay vì existsBy
     */
    @Query(value = "{'title': {$regex: ?0, $options: 'i'}}", count = true)
    long countByTitleIgnoreCase(String title);

    /**
     * Tìm flashcard theo title (case-insensitive)
     */
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    List<Flashcard> findByTitleContainingIgnoreCase(String title);
}
