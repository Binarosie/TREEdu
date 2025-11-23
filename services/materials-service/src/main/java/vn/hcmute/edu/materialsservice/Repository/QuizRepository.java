package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.Model.Quiz;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends MongoRepository<Quiz, String> {

    // Find by title
    Optional<Quiz> findByTitle(String title);

    // Find by topic
    List<Quiz> findByTopic(String topic);

    // Find by difficulty level
    List<Quiz> findByLevel(Integer level);

    // Find by topic and level
    List<Quiz> findByTopicAndLevel(String topic, Integer level);

    // Find by topic with pagination
    Page<Quiz> findByTopic(String topic, Pageable pageable);

    // Find by level with pagination
    Page<Quiz> findByLevel(Integer level, Pageable pageable);

    // Custom query: Find quizzes with minimum question count
    @Query("{ 'questionCount': { $gte: ?0 } }")
    List<Quiz> findQuizzesWithMinQuestions(Integer minQuestions);

    // Custom query: Find quizzes by topic (case-insensitive)
    @Query("{ 'topic': { $regex: ?0, $options: 'i' } }")
    List<Quiz> findByTopicIgnoreCase(String topic);

    // Check if quiz exists by title
    boolean existsByTitle(String title);

    // Count quizzes by topic
    long countByTopic(String topic);
}
