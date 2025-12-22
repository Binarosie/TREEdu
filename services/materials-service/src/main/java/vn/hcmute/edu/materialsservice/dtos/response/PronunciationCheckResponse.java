package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PronunciationCheckResponse {
    private String id;
    private String expectedText;
    private String recognizedText;
    private Integer pronunciationScore;
    private List<PronunciationError> pronunciationErrors;

    @Getter
    @Setter
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
