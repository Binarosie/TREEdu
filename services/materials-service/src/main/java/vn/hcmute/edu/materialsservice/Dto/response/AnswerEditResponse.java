package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerEditResponse {
    private String answerId;
    private String content;
    private Boolean isCorrect;
}
