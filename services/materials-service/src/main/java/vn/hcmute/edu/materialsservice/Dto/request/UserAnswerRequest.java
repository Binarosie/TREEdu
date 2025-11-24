package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.Data;

@Data
public class UserAnswerRequest {
    private String questionId;
    private String selectedAnswerId;
}
