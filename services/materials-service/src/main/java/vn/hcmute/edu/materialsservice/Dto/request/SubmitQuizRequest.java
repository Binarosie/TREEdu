package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequest {
    // ID của lượt làm bài (lấy từ response của API start)
    private String attemptId;

    // Danh sách các câu trả lời user đã chọn
    private List<UserAnswerRequest> answers;
}
