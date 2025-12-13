package vn.hcmute.edu.materialsservice.Dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {

    @NotBlank(message = "tiêu đề không được để trống")
    private String title;


    @NotNull(message = "Level không được để trống")
    @Min(value = 1, message = "Level phải từ 1-6")
    @Max(value = 6, message = "Level phải từ 1-6")
    private Integer level;

    @NotBlank(message = "Topic không được để trống")
    private String topic;

    private String timer;

    @NotNull(message = "Questions list cannot be null")
    @Size(min = 1, message = "Quiz must have at least 1 question")
    @Valid
    private List<QuestionRequest> questions;

}
