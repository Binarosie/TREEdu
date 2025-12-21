package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "pronunciation_checks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PronunciationHistory {
    @Id
    private String id;

    private String expectedText;
    private String recognizedText;
    private Integer pronunciationScore;

    private List<PronunciationError> pronunciationErrors;

    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PronunciationError {
        private String original;
        private String recognized;
        private Integer index;
        private String type;
        private String explanation;
    }
}
