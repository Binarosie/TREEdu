package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hcmute.edu.materialsservice.dtos.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.dtos.request.UserAnswerRequest;
import vn.hcmute.edu.materialsservice.dtos.response.*;
import vn.hcmute.edu.materialsservice.models.*;
import vn.hcmute.edu.materialsservice.repository.QuizAttemptRepository;
import vn.hcmute.edu.materialsservice.repository.QuizRepository;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.services.IStreakService;
import vn.hcmute.edu.materialsservice.services.iQuizAttemptService;
import vn.hcmute.edu.materialsservice.exceptions.ResourceNotFoundException;
import vn.hcmute.edu.materialsservice.services.iUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizAttemptServiceImpl implements iQuizAttemptService {

        private final QuizRepository quizRepository;
        private final QuizAttemptRepository attemptRepository;
        private final vn.hcmute.edu.materialsservice.Mapper.QuizMapper quizMapper;
        private final IStreakService streakService;
        private final UserRepository userRepository;
        private final iUserService userService;
        private final MissionServiceImpl missionService;

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
                attempt.setSubmitted(true);
                attempt.setSubmittedAt(LocalDateTime.now());
                attemptRepository.save(attempt);

                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                // Khai báo các biến Gamification ra ngoài khối block if để map vào Object trả về cuối hàm
                int xpGained = 0;
                boolean isLevelUp = false;
                int finalLevel = 1;

                if (user instanceof Member member) {

                        // 🔥 Cập nhật thông tin Chuỗi ngày học (Nhớ xóa bỏ dòng userRepository.save(member) bên trong hàm updateStreak nhé ông)
                        streakService.updateStreak(member);

                        // ⭐ Tính toán XP nhận được theo thuật toán tỷ lệ chính xác và cấp độ Quiz
                        xpGained = calculateXpGained(correctCount, quiz.getQuestionCount(), quiz.getLevel());

                        int currentXp = member.getXp() != null ? member.getXp() : 0;
                        int oldLevel = member.getLevel() != null ? member.getLevel() : 1;

                        // Cộng dồn XP mới nhận được vào tổng điểm tích lũy của Member
                        int newXp = currentXp + xpGained;
                        member.setXp(newXp);

                        int totalQuiz = member.getTotalQuizCompleted() != null ? member.getTotalQuizCompleted() : 0;
                        member.setTotalQuizCompleted(totalQuiz + 1);

                        int newLevel = userService.calculateLevel(newXp);
                        member.setLevel(newLevel);


                        isLevelUp = newLevel > oldLevel;
                        finalLevel = newLevel;

                        // 💾 LƯU DUY NHẤT MỘT LẦN XUỐNG CƠ SỞ DỮ LIỆU
                        userRepository.save(member);

                        log.info("🎯 User {} nhận được {} XP từ bài Quiz Level {}. Cấp cũ: {}, Cấp mới: {}",
                                userId, xpGained, quiz.getLevel(), oldLevel, newLevel);

                        // 🚀 ============ KÍCH HOẠT NHIỆM VỤ HÀNG NGÀY ============

                        // 1. Tăng tiến trình nhiệm vụ "Làm Quiz bất kỳ" lên 1
                        missionService.fireMissionEvent(userId, vn.hcmute.edu.materialsservice.Enum.EMissionType.DO_QUIZ_ANY, 1);

                        // 2. Tăng tiến trình nhiệm vụ "Tích lũy XP trong ngày" (nếu có kiếm được XP)
                        if (xpGained > 0) {
                                missionService.fireMissionEvent(userId, vn.hcmute.edu.materialsservice.Enum.EMissionType.EARN_DAILY_XP, xpGained);
                        }

                        // 3. Tăng tiến trình nhiệm vụ "Đạt điểm tuyệt đối" (nếu đúng 100% số câu)
                        if (correctCount == quiz.getQuestionCount() && quiz.getQuestionCount() > 0) {
                                // Lưu ý: Cần đảm bảo ông đã thêm GET_PERFECT_SCORE vào file Enum của ông
                                missionService.fireMissionEvent(userId, vn.hcmute.edu.materialsservice.Enum.EMissionType.GET_PERFECT_SCORE, 1);
                        }

                        // ========================================================
                }

                log.info("✅ Quiz submitted: attemptId={}, score={}/{}", attempt.getId(), correctCount, quiz.getQuestionCount());

                return QuizAttemptResponse.builder()
                        .attemptId(attempt.getId())
                        .quizTitle(quiz.getTitle())
                        .score(correctCount)
                        .totalQuestions(quiz.getQuestionCount())
                        .percentage(Math.round((double) correctCount / quiz.getQuestionCount() * 1000) / 10.0)
                        .results(results)
                        .submittedAt(attempt.getSubmittedAt())
                        .xpGained(xpGained)
                        .leveledUp(isLevelUp)
                        .currentLevel(finalLevel)
                        .build();
        }

        private String getAnswerContent(Question question, String answerId) {
                return question.getOptions().stream()
                        .filter(a -> a.getAnswerId().equals(answerId))
                        .map(Answer::getContent)
                        .findFirst()
                        .orElse("Chưa chọn");
        }

        @Override
        public List<QuizAttemptResponse> getUserAttemptHistory(String userId) {
                log.info("📋 Getting submitted attempt history for user: {}", userId);

                List<QuizAttempt> attempts = attemptRepository.findByUserIdAndSubmittedTrueOrderBySubmittedAtDesc(userId);

                log.info("✅ Found {} submitted attempts", attempts.size());

                return attempts.stream()
                        .map(this::mapToResponseSummary)
                        .collect(java.util.stream.Collectors.toList());
        }

        @Override
        public List<QuizAttemptResponse> getUserAttemptsByQuiz(String quizId, String userId) {
                log.info("📋 Getting submitted attempts for quiz: {} and user: {}", quizId, userId);

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
                        // Điền các giá trị mặc định để tránh lỗi vỡ cấu trúc DTO khi xem chi tiết bài cũ
                        .xpGained(0)
                        .leveledUp(false)
                        .currentLevel(1)
                        .build();
        }

        @Override
        public QuizDashboardResponse getAdminDashboardStats() {
                log.info("📊 Fetching admin dashboard statistics...");

                // Cố định múi giờ Việt Nam, tránh lỗi lệch múi giờ UTC gây sai số liệu khi deploy Cloud
                ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
                LocalDateTime startOfDay = LocalDate.now(vnZone).atStartOfDay();
                LocalDateTime now = LocalDateTime.now(vnZone);

                long totalAttempts = attemptRepository.countBySubmittedTrue();
                long attemptsToday = attemptRepository.countBySubmittedAtBetweenAndSubmittedTrue(startOfDay, now);

                List<QuizAttemptRepository.QuizAttemptStats> topRaw = attemptRepository.findTopPopularQuizzes();

                List<QuizDashboardResponse.TopQuizStat> topQuizzes = topRaw.stream().map(item -> {
                        String currentQuizId = item.getQuizId();
                        String quizTitle = "Unknown Quiz";

                        if (currentQuizId != null) {
                                quizTitle = quizRepository.findById(currentQuizId)
                                        .map(Quiz::getTitle)
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

        /**
         * Thuật toán phân cấp tính toán điểm XP dựa vào tỷ lệ chính xác kết hợp độ khó của đề bài.
         */
        private int calculateXpGained(int correctCount, int totalQuestions, int quizLevel) {
                if (totalQuestions <= 0) return 0;

                double accuracy = (double) correctCount / totalQuestions;
                int baseXp;

                if (accuracy == 1.0) {
                        baseXp = 30; // 100% câu đúng
                } else if (accuracy >= 0.7) {
                        baseXp = 20; // 70% -> dưới 100% câu đúng
                } else if (accuracy >= 0.5) {
                        baseXp = 10; // 50% -> dưới 70% câu đúng
                } else {
                        baseXp = 2;  // Điểm khuyến khích nỗ lực học tập
                }

                double multiplier = 1.0;
                if (quizLevel >= 5) {
                        multiplier = 2.0; // Đề khó (Level 5, 6)
                } else if (quizLevel >= 3) {
                        multiplier = 1.5; // Đề trung bình (Level 3, 4)
                }

                return (int) Math.round(baseXp * multiplier);
        }

}
