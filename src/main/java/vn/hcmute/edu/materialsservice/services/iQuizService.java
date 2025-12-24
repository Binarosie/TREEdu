package vn.hcmute.edu.materialsservice.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.materialsservice.dtos.request.GenerateQuizFromFileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.QuizRequest;
import vn.hcmute.edu.materialsservice.dtos.response.QuizEditResponse;
import vn.hcmute.edu.materialsservice.dtos.response.QuizResponse;

import java.io.IOException;
import java.util.List;

public interface iQuizService {

    QuizResponse createQuiz(QuizRequest requestDTO);

    QuizResponse getQuizById(String id);

    Page<QuizResponse> getAllQuizzes(Pageable pageable);

    List<QuizResponse> getQuizzesByTopic(String topic);

    List<QuizResponse> getQuizzesByLevel(Integer level);

    QuizResponse updateQuiz(String id, QuizRequest requestDTO);

    void deleteQuiz(String id);

    List<QuizResponse> searchQuizzesByTopic(String topic);

    QuizEditResponse getQuizForEdit(String id);

    QuizResponse generateQuizFromFile(GenerateQuizFromFileRequest request) throws IOException;

    long countAllQuizzes();
}
