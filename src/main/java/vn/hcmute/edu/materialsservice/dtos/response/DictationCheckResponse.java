package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictationCheckResponse {
    private Double accuracy;
    private Boolean passed;
    private String correctAnswer;
    private List<WordDiff> wordDetails;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class WordDiff {
        private String word;
        private String status; // "CORRECT" hoặc "INCORRECT"
    }
}
