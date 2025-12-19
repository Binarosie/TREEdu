// src/main/java/vn/hcmute/edu/materialsservice/Model/QuizAttempt.java
package vn.hcmute.edu.materialsservice.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quiz_attempt")
public class QuizAttempt {
    @Id
    private String id;

    private String userId;
    private String quizId;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime expiresAt;

    private Integer score;
    private Integer totalQuestions;
    private Integer correctAnswers;

    private List<UserAnswer> answers;
    private boolean submitted;
}
