package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class QuizDashboardResponse {
    private long totalAttempts;       // Tổng số lượt làm bài từ trước tới giờ
    private long attemptsToday;       // Số lượt làm bài trong hôm nay
    private List<TopQuizStat> topQuizzes; // Top những bài quiz hot nhất

    @Data
    @Builder
    public static class TopQuizStat {
        private String quizId;
        private String quizTitle;
        private long attemptCount;
    }
}
