package vn.hcmute.edu.materialsservice.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vn.hcmute.edu.materialsservice.Model.QuizAttempt;

import java.util.List;

public interface QuizAttemptRepository extends MongoRepository<QuizAttempt, String> {

    // Đếm số lượt làm bài của quiz (để check trước khi update/delete)
    long countByQuizId(String quizId);

    // Lấy tất cả lịch sử làm bài của 1 user
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(String userId);

    // Lấy tất cả lịch sử làm bài của 1 user cho 1 quiz cụ thể
    List<QuizAttempt> findByQuizIdAndUserIdOrderByStartedAtDesc(String quizId, String userId);

    // Lấy tất cả attempts của 1 quiz (cho admin/supporter xem thống kê)
    List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(String quizId);
}
