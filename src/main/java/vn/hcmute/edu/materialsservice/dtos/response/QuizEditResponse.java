package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizEditResponse {
    private String id;
    private String title;
    private String topic;
    private Integer level;
    private Integer timer;
    private List<QuestionEditResponse> questions;
    private Integer questionCount;
}
