package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {
    private String questionId;      // ID câu hỏi
    private String selectedAnswerId; // ID đáp án user chọn
}
