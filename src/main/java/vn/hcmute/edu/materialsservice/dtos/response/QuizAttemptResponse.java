    // QuizAttemptResponse.java
    package vn.hcmute.edu.materialsservice.dtos.response;

    import lombok.Builder;
    import lombok.Data;

    import java.time.LocalDateTime;
    import java.util.List;

    @Data
    @Builder
    public class QuizAttemptResponse {
        private String attemptId;
        private String quizTitle;
        private Integer score;
        private Integer totalQuestions;
        private Double percentage;
        private List<QuestionResult> results;
        private LocalDateTime submittedAt;

    @Data
    @Builder
    public static class QuestionResult {
        private String questionId;
        private String content;
        private String selectedAnswer;
        private String correctAnswer;
        private boolean correct;
        private String explanation;
    }
}
