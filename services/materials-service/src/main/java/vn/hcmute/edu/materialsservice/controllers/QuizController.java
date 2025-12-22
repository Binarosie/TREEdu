package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.GenerateQuizFromFileRequest;
import vn.hcmute.edu.materialsservice.dtos.request.QuizRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.dtos.response.*;
import vn.hcmute.edu.materialsservice.services.iQuizAttemptService;
import vn.hcmute.edu.materialsservice.services.iQuizService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * QUIZ CONTROLLER - Với phân quyền rõ ràng
 *
 * PHÂN QUYỀN:
 * - ROLE_MEMBER: Xem quiz (không có đáp án), làm bài, submit, xem lịch sử
 * - ROLE_SUPPORTER: Tất cả quyền của MEMBER + Tạo, Sửa, Xóa quiz + Xem đáp án
 * đúng
 * - ROLE_ADMIN: Full quyền
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

        private final iQuizService quizService;
        private final iQuizAttemptService quizAttemptService;

        // ==================== ROLE_MEMBER + ROLE_SUPPORTER + ROLE_ADMIN
        // ====================

        /**
         * XEM DANH SÁCH QUIZ (Có phân trang)
         *
         * Response: QuizResponse
         * - Guest/ROLE_MEMBER: KHÔNG có explanation
         * - ROLE_ADMIN/SUPPORTER: CÓ explanation
         * PUBLIC API - Guest có thể truy cập
         */
        @GetMapping
        public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAllQuizzes(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt,desc") String[] sort,
                        Authentication authentication) {

                log.info("REST request to get all quizzes - page: {}, size: {}", page, size);

                Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Sort sortBy = Sort.by(direction, sort[0]);

                Pageable pageable = PageRequest.of(page, size, sortBy);
                Page<QuizResponse> response = quizService.getAllQuizzes(pageable);

                // Filter response dựa trên role (nếu đã login)
                if (authentication != null && authentication.isAuthenticated()) {
                        response.getContent().forEach(quiz -> filterQuizResponseByRole(quiz, authentication));
                } else {
                        // Guest: cũng ẩn explanation
                        response.getContent().forEach(quiz -> {
                                if (quiz.getQuestions() != null) {
                                        quiz.getQuestions().forEach(question -> question.setExplanation(null));
                                }
                        });
                }

                return ResponseEntity.ok(ApiResponse.<Page<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        /**
         * XEM CHI TIẾT QUIZ
         *
         * Response: QuizResponse
         * - ROLE_MEMBER: KHÔNG có explanation (để tránh gợi ý đáp án)
         * - ROLE_ADMIN/SUPPORTER: CÓ explanation (để review quiz)
         */
        @GetMapping("/{id}")
        // @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(
                        @PathVariable String id,
                        Authentication authentication) {

                log.info("REST request to get quiz by ID: {}", id);

                QuizResponse response = quizService.getQuizById(id);

                // Filter response dựa trên role
                filterQuizResponseByRole(response, authentication);

                return ResponseEntity.ok(ApiResponse.<QuizResponse>builder()
                                .success(true)
                                .message("Quiz retrieved successfully")
                                .data(response)
                                .build());
        }

        @GetMapping("/topic/{topic}")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByTopic(
                        @PathVariable String topic,
                        Authentication authentication) {

                log.info("REST request to get quizzes by topic: {}", topic);

                List<QuizResponse> response = quizService.getQuizzesByTopic(topic);

                // Filter response dựa trên role
                response.forEach(quiz -> filterQuizResponseByRole(quiz, authentication));

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        @GetMapping("/level/{level}")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByLevel(
                        @PathVariable Integer level,
                        Authentication authentication) {

                log.info("REST request to get quizzes by level: {}", level);

                List<QuizResponse> response = quizService.getQuizzesByLevel(level);

                // Filter response dựa trên role
                response.forEach(quiz -> filterQuizResponseByRole(quiz, authentication));

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> searchQuizzes(
                        @RequestParam String topic,
                        Authentication authentication) {

                log.info("REST request to search quizzes by topic: {}", topic);

                List<QuizResponse> response = quizService.searchQuizzesByTopic(topic);

                // Filter response dựa trên role
                response.forEach(quiz -> filterQuizResponseByRole(quiz, authentication));

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Search completed successfully")
                                .data(response)
                                .build());
        }

        @PostMapping("/{quizId}/start")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<StartQuizResponse>> startQuiz(
                        @PathVariable String quizId,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                log.info("User {} started quiz {}", userId, quizId);

                StartQuizResponse response = quizAttemptService.startQuiz(quizId, userId);

                // ẨN EXPLANATION cho MỌI USER khi làm bài (kể cả ADMIN/SUPPORTER)
                if (response.getQuiz() != null && response.getQuiz().getQuestions() != null) {
                        response.getQuiz().getQuestions().forEach(question -> question.setExplanation(null));
                }

                return ResponseEntity.ok(ApiResponse.<StartQuizResponse>builder()
                                .success(true)
                                .message("Bắt đầu làm bài thành công!")
                                .data(response)
                                .build());
        }

        @PostMapping("/{quizId}/submit")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitQuiz(
                        @PathVariable String quizId,
                        @RequestBody SubmitQuizRequest request,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                log.info("User {} submitted quiz {}", userId, quizId);

                QuizAttemptResponse response = quizAttemptService.submitQuiz(quizId, request, userId);

                return ResponseEntity.ok(ApiResponse.<QuizAttemptResponse>builder()
                                .success(true)
                                .message("Nộp bài thành công!")
                                .data(response)
                                .build());
        }

        /**
         * XEM LỊCH Sử LÀM BÀI CỦA USER
         *
         * Lấy tất cả các lần làm quiz của user hiện tại
         */
        @GetMapping("/my-attempts")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<List<QuizAttemptResponse>>> getMyAttempts(
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                log.info("User {} getting attempt history", userId);

                List<QuizAttemptResponse> response = quizAttemptService.getUserAttemptHistory(userId);

                return ResponseEntity.ok(ApiResponse.<List<QuizAttemptResponse>>builder()
                                .success(true)
                                .message("Lịch sử làm bài của bạn")
                                .data(response)
                                .build());
        }

        /**
         * XEM LỊCH Sử LÀM BÀI CHO 1 QUIZ CỤ THỂ
         *
         * Lấy tất cả các lần user làm một quiz cụ thể
         */
        @GetMapping("/{quizId}/my-attempts")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<List<QuizAttemptResponse>>> getMyAttemptsByQuiz(
                        @PathVariable String quizId,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                log.info("User {} getting attempts for quiz {}", userId, quizId);

                List<QuizAttemptResponse> response = quizAttemptService.getUserAttemptsByQuiz(quizId, userId);

                return ResponseEntity.ok(ApiResponse.<List<QuizAttemptResponse>>builder()
                                .success(true)
                                .message("Lịch sử làm bài quiz này")
                                .data(response)
                                .build());
        }

        /**
         * XEM CHI TIẾT 1 LẦN LÀM BÀI
         *
         * Xem lại kết quả, đáp án của một lần làm bài cũ
         */
        @GetMapping("/attempts/{attemptId}")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> getAttemptDetail(
                        @PathVariable String attemptId,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();
                String userId = userDetails.getUser().getId().toString();

                log.info("User {} viewing attempt {}", userId, attemptId);

                QuizAttemptResponse response = quizAttemptService.getAttemptDetail(attemptId, userId);

                return ResponseEntity.ok(ApiResponse.<QuizAttemptResponse>builder()
                                .success(true)
                                .message("Chi tiết lần làm bài")
                                .data(response)
                                .build());
        }

        // ==================== ROLE_SUPPORTER + ROLE_ADMIN ONLY ====================

        /**
         * TẠO QUIZ MỚI
         *
         * Chỉ SUPPORTER và ADMIN
         */
        @PostMapping
        @PreAuthorize("hasAnyRole('ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
                        @Valid @RequestBody QuizRequest requestDTO,
                        Authentication authentication) {

                log.info("REST request to create quiz: {}", requestDTO.getTitle());

                QuizResponse response = quizService.createQuiz(requestDTO);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.<QuizResponse>builder()
                                                .success(true)
                                                .message("Quiz created successfully")
                                                .data(response)
                                                .build());
        }

        /**
         * CẬP NHẬT QUIZ
         *
         * Chỉ SUPPORTER và ADMIN
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
                        @PathVariable String id,
                        @Valid @RequestBody QuizRequest requestDTO,
                        Authentication authentication) {

                log.info("REST request to update quiz with ID: {}", id);

                QuizResponse response = quizService.updateQuiz(id, requestDTO);

                return ResponseEntity.ok(ApiResponse.<QuizResponse>builder()
                                .success(true)
                                .message("Quiz updated successfully")
                                .data(response)
                                .build());
        }

        /**
         * XÓA QUIZ
         *
         * Chỉ SUPPORTER và ADMIN
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteQuiz(
                        @PathVariable String id,
                        Authentication authentication) {

                log.info("REST request to delete quiz with ID: {}", id);

                quizService.deleteQuiz(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Quiz deleted successfully")
                                .build());
        }

        /**
         * LẤY QUIZ ĐỂ EDIT
         *
         * Response: QuizEditResponse (CÓ isCorrect)
         * Chỉ SUPPORTER và ADMIN
         *
         * ĐÂY LÀ ENDPOINT QUAN TRỌNG:
         * - USER không được truy cập (sẽ thấy đáp án đúng)
         * - Chỉ SUPPORTER review quiz mới dùng
         */
        @GetMapping("/edit/{id}")
        @PreAuthorize("hasAnyRole('ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizEditResponse>> getQuizForEdit(
                        @PathVariable String id,
                        Authentication authentication) {

                log.info("REST request to get quiz for editing by ID: {}", id);

                QuizEditResponse response = quizService.getQuizForEdit(id);

                return ResponseEntity.ok(ApiResponse.<QuizEditResponse>builder()
                                .success(true)
                                .message("Quiz retrieved for editing")
                                .data(response)
                                .build());
        }


        @PostMapping("/generate-from-file")
       // @PreAuthorize("hasAnyRole('ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> generateQuizFromFile(
                        @ModelAttribute GenerateQuizFromFileRequest request
        ) throws IOException {

                log.info("REST request to generate quiz from file");

                QuizResponse quiz = quizService.generateQuizFromFile(request);

                return ResponseEntity.ok(ApiResponse.success(quiz));
        }

        @GetMapping("/admin/statistics")
        @PreAuthorize("hasRole('ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<Object>> getQuizStatistics() {
                log.info("=== START getQuizStatistics ===");
                try {
                        log.info("Counting total quizzes...");
                        long totalQuizzes = quizService.countAllQuizzes();
                        log.info("Total quizzes: {}", totalQuizzes);
                        Map<String, Object> stats = new java.util.HashMap<>();
                        stats.put("totalQuizzes", totalQuizzes);
                        stats.put("message", "Quiz statistics retrieved successfully");
                        log.info("Stats map created: {}", stats);
                        ApiResponse<Object> response = ApiResponse.builder()
                                        .success(true)
                                        .message("Statistics retrieved successfully")
                                        .data(stats)
                                        .build();
                        log.info("ApiResponse built successfully");
                        log.info("=== END getQuizStatistics SUCCESS ===");
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        log.error("=== ERROR in getQuizStatistics ===");
                        log.error("Error type: {}", e.getClass().getName());
                        log.error("Error message: {}", e.getMessage());
                        log.error("Stack trace:", e);
                        throw e;
                }
        }

        // ==================== HELPER METHODS ====================

        /**
         * Filter quiz response dựa trên role:
         * - Guest (chưa login): Ẩn explanation
         * - ROLE_MEMBER: Ẩn explanation (tránh gợi ý đáp án)
         * - ROLE_ADMIN/SUPPORTER: Hiển thị đầy đủ (để review quiz)
         */
        private void filterQuizResponseByRole(QuizResponse quiz, Authentication authentication) {
                if (quiz == null || quiz.getQuestions() == null) {
                        return;
                }

                // Guest (chưa login): ẩn explanation
                if (authentication == null || !authentication.isAuthenticated()) {
                        quiz.getQuestions().forEach(question -> question.setExplanation(null));
                        return;
                }

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication
                                .getPrincipal();

                // Kiểm tra xem user có phải là ADMIN hoặc SUPPORTER không
                boolean isAdminOrSupporter = userDetails.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                                                || auth.getAuthority().equals("ROLE_SUPPORTER"));

                // Nếu KHÔNG phải ADMIN/SUPPORTER (tức là MEMBER): ẩn explanation
                if (!isAdminOrSupporter) {
                        quiz.getQuestions().forEach(question -> question.setExplanation(null));
                }
                // ROLE_ADMIN và ROLE_SUPPORTER: giữ nguyên (có explanation)
        }
        @GetMapping("/admin/stats")
        @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
        public ResponseEntity<QuizDashboardResponse> getDashboardStats() {
                return ResponseEntity.ok(quizAttemptService.getAdminDashboardStats());
        }
}
