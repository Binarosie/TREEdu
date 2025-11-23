// QuizAttemptServiceImpl.java
package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Dto.request.SubmitQuizRequest;

import vn.hcmute.edu.materialsservice.Dto.request.UserAnswerRequest;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Model.*;
import vn.hcmute.edu.materialsservice.Repository.QuizAttemptRepository;
import vn.hcmute.edu.materialsservice.Repository.QuizRepository;
import vn.hcmute.edu.materialsservice.Service.QuizAttemptService;
import vn.hcmute.edu.materialsservice.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;

    // QuizAttemptServiceImpl.java (chỉ thay đoạn này)
    @Override
    public StartQuizResponse startQuiz(String quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(quiz.getTimer());

        QuizAttempt attempt = QuizAttempt.builder()
                .sessionId(java.util.UUID.randomUUID().toString()) // Tạo ID tạm
                .quizId(quizId)
                .startedAt(now)
                .expiresAt(expiresAt)
                .totalQuestions(quiz.getQuestionCount())
                .submitted(false)
                .answers(new ArrayList<>())
                .build();

        QuizAttempt saved = attemptRepository.save(attempt);

        return StartQuizResponse.builder()
                .attemptId(saved.getId())
                .quiz(quiz)
                .timeRemainingSeconds(java.time.Duration.between(now, expiresAt).getSeconds())
                .expiresAt(expiresAt.toString())
                .build();
    }

    @Override
    public QuizAttemptResponse submitQuiz(String quizId, SubmitQuizRequest request) {
        // Không cần userId nữa
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        QuizAttempt attempt = attemptRepository.findById(request.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

        if (attempt.isSubmitted()) {
            throw new IllegalStateException("Bài đã được nộp rồi!");
        }

        if (LocalDateTime.now().isAfter(attempt.getExpiresAt())) {
            attempt.setSubmitted(true);
            attempt.setSubmittedAt(LocalDateTime.now());
            attempt.setScore(0);
            attemptRepository.save(attempt);
            throw new IllegalStateException("Hết thời gian làm bài!");
        }

        // Tính điểm như cũ...
        int correctCount = 0;
        List<QuizAttemptResponse.QuestionResult> results = new ArrayList<>();

        for (UserAnswerRequest ua : request.getAnswers()) {
            Question q = quiz.getQuestions().stream()
                    .filter(question -> question.getQuestionId().equals(ua.getQuestionId()))
                    .findFirst()
                    .orElse(null);

            if (q == null) continue;

            Answer correctAnswer = q.getCorrectAnswer();
            boolean isCorrect = correctAnswer != null && correctAnswer.getAnswerId().equals(ua.getSelectedAnswerId());
            if (isCorrect) correctCount++;

            results.add(QuizAttemptResponse.QuestionResult.builder()
                    .questionId(q.getQuestionId())
                    .content(q.getContent())
                    .selectedAnswer(getAnswerContent(q, ua.getSelectedAnswerId()))
                    .correctAnswer(correctAnswer != null ? correctAnswer.getContent() : "N/A")
                    .correct(isCorrect)
                    .explanation(q.getExplanation())
                    .build());
        }

        attempt.setScore(correctCount);
        attempt.setCorrectAnswers(correctCount);
        attempt.setSubmitted(true);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        return QuizAttemptResponse.builder()
                .attemptId(attempt.getId())
                .quizTitle(quiz.getTitle())
                .score(correctCount)
                .totalQuestions(quiz.getQuestionCount())
                .percentage(Math.round((double) correctCount / quiz.getQuestionCount() * 1000) / 10.0)
                .results(results)
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }

    private String getAnswerContent(Question question, String answerId) {
        return question.getOptions().stream()
                .filter(a -> a.getAnswerId().equals(answerId))
                .map(Answer::getContent)
                .findFirst()
                .orElse("Chưa chọn");
    }
}
