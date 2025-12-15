// StartQuizResponse.java
package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartQuizResponse {
    private String attemptId;
    private QuizResponse quiz; // Đổi từ Model Quiz sang QuizResponse (không có isCorrect)
    private Long timeRemainingSeconds; // giây còn lại
    private String expiresAt;
}
