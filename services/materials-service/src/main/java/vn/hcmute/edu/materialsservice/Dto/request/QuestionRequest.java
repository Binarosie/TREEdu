package vn.hcmute.edu.materialsservice.Dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {
    @NotBlank(message = "Question content is required")
    @Size(min = 5, max = 1000, message = "Question content must be between 5 and 1000 characters")
    private String content;

    @NotNull(message = "Options list cannot be null")
    @Size(min = 2, max = 4, message = "Question must have between 2 and 4 options")
    @Valid
    private List<AnswerRequest> options;

    @Size(max = 2000, message = "Explanation cannot exceed 2000 characters")
    private String explanation;
}
