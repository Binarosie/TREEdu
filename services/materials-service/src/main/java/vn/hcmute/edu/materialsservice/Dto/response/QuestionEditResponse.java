package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEditResponse {
    private String questionId;
    private String content;
    private List<AnswerEditResponse> options;
    private String explanation;
}
