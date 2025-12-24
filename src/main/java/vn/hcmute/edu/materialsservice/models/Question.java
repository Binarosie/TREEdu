package vn.hcmute.edu.materialsservice.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    @JsonIgnore // Ẩn method này khi serialize để không lộ đáp án đúng
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
