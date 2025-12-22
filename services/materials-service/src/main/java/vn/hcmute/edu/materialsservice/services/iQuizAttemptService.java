// QuizAttemptService.java
package vn.hcmute.edu.materialsservice.services;

import vn.hcmute.edu.materialsservice.dtos.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.dtos.response.QuizAttemptResponse;
import vn.hcmute.edu.materialsservice.dtos.response.QuizDashboardResponse;
import vn.hcmute.edu.materialsservice.dtos.response.StartQuizResponse;

import java.util.List;

public interface iQuizAttemptService {

    // Bắt đầu làm bài quiz (cần userId để track)
    StartQuizResponse startQuiz(String quizId, String userId);

    // Nộp bài quiz (cần userId để track)
    QuizAttemptResponse submitQuiz(String quizId, SubmitQuizRequest request, String userId);

    // Lấy lịch sử làm bài của user
    List<QuizAttemptResponse> getUserAttemptHistory(String userId);

    // Lấy lịch sử làm bài của user cho 1 quiz cụ thể
    List<QuizAttemptResponse> getUserAttemptsByQuiz(String quizId, String userId);

    // Xem chi tiết 1 lần làm bài
    QuizAttemptResponse getAttemptDetail(String attemptId, String userId);

    // // Đếm tổng số lượt làm quiz trong hệ thống
    // long countAllAttempts();


    QuizDashboardResponse getAdminDashboardStats();
}
