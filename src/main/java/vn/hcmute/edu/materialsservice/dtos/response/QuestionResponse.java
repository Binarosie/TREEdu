package vn.hcmute.edu.materialsservice.dtos.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QuestionResponse {

    private String questionId;

    private String content;

    private List<AnswerResponse> options;

    private String explanation;
}
