// SubmitQuizRequest.java
package vn.hcmute.edu.materialsservice.Dto.request;

import lombok.Data;
import java.util.List;

@Data
public class SubmitQuizRequest {
    private String attemptId;
    private List<UserAnswerRequest> answers;
}

