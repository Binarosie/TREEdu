package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WordCheckResponse {
    private String wordId;
    private boolean correct;
    private String correctAnswer;
    private String userAnswer;
    private FlashcardProgressResponse progress; // null nếu sai (chưa cập nhật progress)
}
