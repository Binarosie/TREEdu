package vn.hcmute.edu.materialsservice.Dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class AnswerRequest {

    @NotBlank(message = "Answer content is required")
    @Size(min = 1, max = 500, message = "Answer content must be between 1 and 500 characters")
    private String content;

    @NotNull(message = "isCorrect flag is required")
    private Boolean isCorrect;
}
