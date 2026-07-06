// StartQuizResponse.java
package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StartQuizResponse {
    private String attemptId;
    private QuizResponse quiz;
    private Long timeRemainingSeconds;
    private String expiresAt;
}
