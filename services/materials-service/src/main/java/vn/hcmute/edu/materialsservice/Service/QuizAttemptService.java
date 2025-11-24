// QuizAttemptService.java
package vn.hcmute.edu.materialsservice.Service;

import vn.hcmute.edu.materialsservice.Dto.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.QuizAttemptResponse;
import vn.hcmute.edu.materialsservice.Dto.response.StartQuizResponse;

public interface QuizAttemptService {
    StartQuizResponse startQuiz(String quizId);
    QuizAttemptResponse submitQuiz(String quizId, SubmitQuizRequest request);
}
