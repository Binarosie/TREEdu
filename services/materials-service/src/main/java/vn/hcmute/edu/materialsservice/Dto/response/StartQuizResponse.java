// StartQuizResponse.java
package vn.hcmute.edu.materialsservice.Dto.response;

import lombok.Builder;
import lombok.Data;
import vn.hcmute.edu.materialsservice.Model.Quiz;

@Data
@Builder
public class StartQuizResponse {
    private String attemptId;
    private Quiz quiz;
    private Long timeRemainingSeconds; // giây còn lại
    private String expiresAt;
}
