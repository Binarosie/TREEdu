package vn.hcmute.edu.materialsservice.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponse;
import vn.hcmute.edu.materialsservice.Dto.response.QuizResponseWithQuestion;

import java.util.List;

public interface QuizService {


    QuizResponseWithQuestion createQuiz(QuizRequest requestDTO);

    QuizResponseWithQuestion getQuizById(String id);

    Page<QuizResponseWithQuestion> getQuizWithQuestion(Pageable pageable);

    Page<QuizResponse> getAllQuizzes(Pageable pageable);


    List<QuizResponseWithQuestion> getQuizzesByTopic(String topic);


    List<QuizResponseWithQuestion> getQuizzesByLevel(Integer level);

    QuizResponseWithQuestion updateQuiz(String id, QuizRequest requestDTO);

    void deleteQuiz(String id);

    List<QuizResponseWithQuestion> searchQuizzesByTopic(String topic);
}
