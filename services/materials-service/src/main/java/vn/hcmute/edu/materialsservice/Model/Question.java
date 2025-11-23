package vn.hcmute.edu.materialsservice.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Builder.Default
    private String questionId = UUID.randomUUID().toString();
    private String content;
    private List<Answer> options;
    private String explanation;

    public Answer getCorrectAnswer() {
        return options.stream()
                .filter(Answer::getIsCorrect)
                .findFirst()
                .orElse(null);
    }

    // Validation helper
    public boolean isValid() {
        if (options == null || options.size() < 2 || options.size() > 4) {
            return false;
        }

        long correctAnswersCount = options.stream()
                .filter(Answer::getIsCorrect)
                .count();

        return correctAnswersCount == 1;
    }
}
