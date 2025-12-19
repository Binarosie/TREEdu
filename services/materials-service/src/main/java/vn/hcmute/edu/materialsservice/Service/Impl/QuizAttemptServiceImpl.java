
package vn.hcmute.edu.materialsservice.Service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.Dto.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.Dto.request.UserAnswerRequest;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Model.*;
import vn.hcmute.edu.materialsservice.Repository.QuizAttemptRepository;
import vn.hcmute.edu.materialsservice.Repository.QuizRepository;
import vn.hcmute.edu.materialsservice.Service.QuizAttemptService;
import vn.hcmute.edu.materialsservice.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements QuizAttemptService {

        private final QuizRepository quizRepository;
        private final QuizAttemptRepository attemptRepository;
        private final vn.hcmute.edu.materialsservice.Mapper.QuizMapper quizMapper;

        @Override
        public StartQuizResponse startQuiz(String quizId, String userId) {
                Quiz quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresAt = now.plusMinutes(quiz.getTimer());

                QuizAttempt attempt = QuizAttempt.builder()
                        .userId(userId)
                        .quizId(quizId)
                        .startedAt(now)
                        .expiresAt(expiresAt)
                        .totalQuestions(quiz.getQuestionCount())
                        .submitted(false)
                        .answers(new ArrayList<>())
                        .build();

                QuizAttempt saved = attemptRepository.save(attempt);
                log.info("✅ Started quiz attempt: {} for user: {}", saved.getId(), userId);

                QuizResponse quizResponse = quizMapper.toResponse(quiz);

                return StartQuizResponse.builder()
                        .attemptId(saved.getId())
                        .quiz(quizResponse)
                        .timeRemainingSeconds(java.time.Duration.between(now, expiresAt).getSeconds())
                        .expiresAt(expiresAt.toString())
                        .build();
        }

        @Override
        public QuizAttemptResponse submitQuiz(String quizId, SubmitQuizRequest request, String userId) {
                Quiz quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

                QuizAttempt attempt = attemptRepository.findById(request.getAttemptId())
                        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

                if (!attempt.getUserId().equals(userId)) {
                        throw new IllegalStateException("Bạn không có quyền nộp bài này!");
                }

                if (attempt.isSubmitted()) {
                        throw new IllegalStateException("Bài đã được nộp rồi!");
                }

                boolean isTimeout = LocalDateTime.now().isAfter(attempt.getExpiresAt());
                int correctCount = 0;
                List<QuizAttemptResponse.QuestionResult> results = new ArrayList<>();

                for (Question q : quiz.getQuestions()) {
                        UserAnswerRequest userAnswer = request.getAnswers().stream()
                                .filter(ua -> ua.getQuestionId().equals(q.getQuestionId()))
                                .findFirst()
                                .orElse(null);

                        Answer correctAnswer = q.getCorrectAnswer();
                        boolean isCorrect = false;
                        String selectedAnswerContent = "Chưa trả lời";

                        if (userAnswer != null && userAnswer.getSelectedAnswerId() != null) {
                                isCorrect = correctAnswer != null &&
                                        correctAnswer.getAnswerId().equals(userAnswer.getSelectedAnswerId());
                                selectedAnswerContent = getAnswerContent(q, userAnswer.getSelectedAnswerId());
                        }

                        if (isCorrect)
                                correctCount++;

                        results.add(QuizAttemptResponse.QuestionResult.builder()
                                .questionId(q.getQuestionId())
                                .content(q.getContent())
                                .selectedAnswer(selectedAnswerContent)
                                .correctAnswer(correctAnswer != null ? correctAnswer.getContent() : "N/A")
                                .correct(isCorrect)
                                .explanation(q.getExplanation())
                                .build());
                }

                List<UserAnswer> userAnswers = request.getAnswers().stream()
                        .map(ua -> UserAnswer.builder()
                                .questionId(ua.getQuestionId())
                                .selectedAnswerId(ua.getSelectedAnswerId())
                                .correct(results.stream()
                                        .filter(r -> r.getQuestionId().equals(ua.getQuestionId()))
                                        .findFirst()
                                        .map(QuizAttemptResponse.QuestionResult::isCorrect)
                                        .orElse(false))
                                .build())
                        .collect(java.util.stream.Collectors.toList());

                attempt.setAnswers(userAnswers);
                attempt.setScore(correctCount);
                attempt.setCorrectAnswers(correctCount);
                attempt.setSubmitted(true); // ✅ Đánh dấu đã submit
                attempt.setSubmittedAt(LocalDateTime.now());
                attemptRepository.save(attempt);

                log.info("✅ Quiz submitted: attemptId={}, score={}/{}", attempt.getId(), correctCount, quiz.getQuestionCount());

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

        // ✅ FIX: Chỉ lấy bài ĐÃ SUBMIT
        @Override
        public List<QuizAttemptResponse> getUserAttemptHistory(String userId) {
                log.info("📋 Getting submitted attempt history for user: {}", userId);

                // Dùng method mới - chỉ lấy submitted=true
                List<QuizAttempt> attempts = attemptRepository.findByUserIdAndSubmittedTrueOrderBySubmittedAtDesc(userId);

                log.info("✅ Found {} submitted attempts", attempts.size());

                return attempts.stream()
                        .map(this::mapToResponseSummary)
                        .collect(java.util.stream.Collectors.toList());
        }

        // ✅ FIX: Chỉ lấy bài ĐÃ SUBMIT theo quiz
        @Override
        public List<QuizAttemptResponse> getUserAttemptsByQuiz(String quizId, String userId) {
                log.info("📋 Getting submitted attempts for quiz: {} and user: {}", quizId, userId);

                // Dùng method mới - chỉ lấy submitted=true
                List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndUserIdAndSubmittedTrueOrderBySubmittedAtDesc(
                        quizId, userId);

                log.info("✅ Found {} submitted attempts for this quiz", attempts.size());

                return attempts.stream()
                        .map(this::mapToResponseSummary)
                        .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public QuizAttemptResponse getAttemptDetail(String attemptId, String userId) {
                QuizAttempt attempt = attemptRepository.findById(attemptId)
                        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

                if (!attempt.getUserId().equals(userId)) {
                        throw new IllegalStateException("Bạn không có quyền xem lịch sử này!");
                }

                // ✅ Kiểm tra xem bài đã submit chưa
                if (!attempt.isSubmitted()) {
                        throw new IllegalStateException("Bài này chưa được nộp!");
                }

                Quiz quiz = quizRepository.findById(attempt.getQuizId())
                        .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

                List<QuizAttemptResponse.QuestionResult> results = new ArrayList<>();

                if (attempt.getAnswers() != null) {
                        for (UserAnswer userAns : attempt.getAnswers()) {
                                Question q = quiz.getQuestions().stream()
                                        .filter(question -> question.getQuestionId().equals(userAns.getQuestionId()))
                                        .findFirst()
                                        .orElse(null);

                                if (q != null) {
                                        Answer correctAnswer = q.getCorrectAnswer();
                                        boolean isCorrect = correctAnswer != null &&
                                                correctAnswer.getAnswerId().equals(userAns.getSelectedAnswerId());

                                        results.add(QuizAttemptResponse.QuestionResult.builder()
                                                .questionId(q.getQuestionId())
                                                .content(q.getContent())
                                                .selectedAnswer(getAnswerContent(q, userAns.getSelectedAnswerId()))
                                                .correctAnswer(correctAnswer != null ? correctAnswer.getContent() : "N/A")
                                                .correct(isCorrect)
                                                .explanation(q.getExplanation())
                                                .build());
                                }
                        }
                }

                return QuizAttemptResponse.builder()
                        .attemptId(attempt.getId())
                        .quizTitle(quiz.getTitle())
                        .score(attempt.getScore())
                        .totalQuestions(attempt.getTotalQuestions())
                        .percentage(Math.round((double) attempt.getScore() / attempt.getTotalQuestions() * 1000) / 10.0)
                        .results(results)
                        .submittedAt(attempt.getSubmittedAt())
                        .build();
        }

        @Override
        public QuizDashboardResponse getAdminDashboardStats() {
                log.info("📊 Fetching admin dashboard statistics...");
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime now = LocalDateTime.now();

                long totalAttempts = attemptRepository.countBySubmittedTrue();
                long attemptsToday = attemptRepository.countBySubmittedAtBetweenAndSubmittedTrue(startOfDay, now);

                // Lấy danh sách raw từ Repository (đã sửa thành Class QuizAttemptStats)
                List<QuizAttemptRepository.QuizAttemptStats> topRaw = attemptRepository.findTopPopularQuizzes();

                List<QuizDashboardResponse.TopQuizStat> topQuizzes = topRaw.stream().map(item -> {
                        // Lấy ID từ object item (Lúc này chắc chắn không null nếu DB có dữ liệu)
                        String currentQuizId = item.getQuizId();
                        String quizTitle = "Unknown Quiz";

                        if (currentQuizId != null) {
                                quizTitle = quizRepository.findById(currentQuizId)
                                        .map(quiz -> quiz.getTitle())
                                        .orElse("Unknown Quiz (Deleted)");
                        } else {
                                log.warn("⚠️ Found aggregation result with null quizId");
                        }

                        return QuizDashboardResponse.TopQuizStat.builder()
                                .quizId(currentQuizId)
                                .quizTitle(quizTitle)
                                .attemptCount(item.getCount())
                                .build();
                }).collect(java.util.stream.Collectors.toList());

                return QuizDashboardResponse.builder()
                        .totalAttempts(totalAttempts)
                        .attemptsToday(attemptsToday)
                        .topQuizzes(topQuizzes)
                        .build();
        }


        private QuizAttemptResponse mapToResponseSummary(QuizAttempt attempt) {
                Quiz quiz = quizRepository.findById(attempt.getQuizId()).orElse(null);
                String quizTitle = quiz != null ? quiz.getTitle() : "Quiz đã bị xóa";

                return QuizAttemptResponse.builder()
                        .attemptId(attempt.getId())
                        .quizTitle(quizTitle)
                        .score(attempt.getScore())
                        .totalQuestions(attempt.getTotalQuestions())
                        .percentage(attempt.getScore() != null && attempt.getTotalQuestions() != null
                                ? Math.round((double) attempt.getScore() / attempt.getTotalQuestions() * 1000) / 10.0
                                : 0.0)
                        .submittedAt(attempt.getSubmittedAt())
                        .build();
        }
}
