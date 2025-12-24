package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.Quiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    // Find by ID (chỉ lấy quiz chưa bị xóa)
    @Query("{ '_id': ?0, 'deleted': { $ne: true } }")
    Optional<Quiz> findById(String id);

    // Find all (chỉ lấy quiz chưa bị xóa)
    @Query("{ 'deleted': { $ne: true } }")
    Page<Quiz> findAll(Pageable pageable);

    // Find by title
    @Query("{ 'title': ?0, 'deleted': { $ne: true } }")
    Optional<Quiz> findByTitle(String title);

    // Find by topic
    @Query("{ 'topic': ?0, 'deleted': { $ne: true } }")
    List<Quiz> findByTopic(String topic);

    // Find by difficulty level
    @Query("{ 'level': ?0, 'deleted': { $ne: true } }")
    List<Quiz> findByLevel(Integer level);

    // Find by topic and level
    @Query("{ 'topic': ?0, 'level': ?1, 'deleted': { $ne: true } }")
    List<Quiz> findByTopicAndLevel(String topic, Integer level);

    // Find by topic with pagination
    @Query("{ 'topic': ?0, 'deleted': { $ne: true } }")
    Page<Quiz> findByTopic(String topic, Pageable pageable);

    // Find by level with pagination
    @Query("{ 'level': ?0, 'deleted': { $ne: true } }")
    Page<Quiz> findByLevel(Integer level, Pageable pageable);

    // Custom query: Find quizzes with minimum question count
    @Query("{ 'questionCount': { $gte: ?0 }, 'deleted': { $ne: true } }")
    List<Quiz> findQuizzesWithMinQuestions(Integer minQuestions);

    // Custom query: Find quizzes by topic (case-insensitive)
    @Query("{ 'topic': { $regex: ?0, $options: 'i' }, 'deleted': { $ne: true } }")
    List<Quiz> findByTopicIgnoreCase(String topic);

    // Fuzzy search: Tìm quiz theo topic với regex pattern cho fuzzy matching
    @Query("{ 'topic': { $regex: ?0, $options: 'i' }, 'deleted': { $ne: true } }")
    List<Quiz> findByTopicFuzzy(String regexPattern);

    // Check if quiz exists by title (chỉ check quiz chưa bị xóa)
    @Query(value = "{ 'title': ?0, 'deleted': { $ne: true } }", exists = true)
    boolean existsByTitle(String title);

    // Check if quiz exists by id (chỉ check quiz chưa bị xóa)
    @Query(value = "{ '_id': ?0, 'deleted': { $ne: true } }", exists = true)
    boolean existsById(String id);

    // Count quizzes by topic
    @Query(value = "{ 'topic': ?0, 'deleted': { $ne: true } }", count = true)
    long countByTopic(String topic);
}
