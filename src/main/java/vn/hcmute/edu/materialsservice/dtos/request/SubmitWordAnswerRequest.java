package vn.hcmute.edu.materialsservice.dtos.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SubmitWordAnswerRequest {
    @NotBlank(message = "wordId không được để trống")
    private String wordId;

    @NotBlank(message = "userAnswer không được để trống")
    private String userAnswer;
}
