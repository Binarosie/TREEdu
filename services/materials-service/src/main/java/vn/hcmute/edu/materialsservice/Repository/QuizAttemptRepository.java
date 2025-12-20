package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hcmute.edu.materialsservice.Model.QuizAttempt;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    // ... (Giữ nguyên các hàm find/count cũ) ...
    long countByQuizId(String quizId);
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(String userId);
    List<QuizAttempt> findByQuizIdAndUserIdOrderByStartedAtDesc(String quizId, String userId);
    List<QuizAttempt> findByUserIdAndSubmittedTrueOrderBySubmittedAtDesc(String userId);
    List<QuizAttempt> findByQuizIdAndUserIdAndSubmittedTrueOrderBySubmittedAtDesc(String quizId, String userId);
    List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(String quizId);

    // --- DASHBOARD STATS ---

    long countBySubmittedTrue();

    long countBySubmittedAtBetweenAndSubmittedTrue(LocalDateTime start, LocalDateTime end);


    @Aggregation(pipeline = {
            "{ '$match': { 'submitted': true } }",
            "{ '$group': { '_id': '$quizId', 'count': { '$sum': 1 } } }",
            "{ '$sort': { 'count': -1 } }",
            "{ '$limit': 5 }",
            "{ '$project': { 'quizId': '$_id', 'count': 1, '_id': 0 } }"
    })
    List<QuizAttemptStats> findTopPopularQuizzes();

    // Dùng Class thay vì Interface để mapping chính xác hơn
    class QuizAttemptStats {
        private String quizId;
        private long count;

        // Getter & Setter bắt buộc phải có để Mongo map dữ liệu
        public String getQuizId() { return quizId; }
        public void setQuizId(String quizId) { this.quizId = quizId; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
