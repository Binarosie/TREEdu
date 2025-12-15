package vn.hcmute.edu.materialsservice.Controller;

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
import vn.hcmute.edu.materialsservice.Dto.request.GenerateQuizFromFileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Service.QuizAttemptService;
import vn.hcmute.edu.materialsservice.Service.QuizService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * QUIZ CONTROLLER - Với phân quyền rõ ràng
 *
 * PHÂN QUYỀN:
 * - ROLE_MEMBER: Xem quiz (không có đáp án), làm bài, submit, xem lịch sử
 * - ROLE_SUPPORTER: Tất cả quyền của MEMBER + Tạo, Sửa, Xóa quiz + Xem đáp án đúng
 * - ROLE_ADMIN: Full quyền
 */
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

        private final QuizService quizService;
        private final QuizAttemptService quizAttemptService;

        // ==================== ROLE_MEMBER + ROLE_SUPPORTER + ROLE_ADMIN ====================

        /**
         * XEM DANH SÁCH QUIZ (Có phân trang)
         *
         * Response: QuizResponse (KHÔNG có isCorrect)
         * PUBLIC API - Guest có thể truy cập
         */
        @GetMapping
        public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAllQuizzes(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

                log.info("REST request to get all quizzes - page: {}, size: {}", page, size);

                Sort.Direction direction = sort.length > 1 && sort[1].equalsIgnoreCase("asc")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;
                Sort sortBy = Sort.by(direction, sort[0]);

                Pageable pageable = PageRequest.of(page, size, sortBy);
                Page<QuizResponse> response = quizService.getAllQuizzes(pageable);

                return ResponseEntity.ok(ApiResponse.<Page<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        /**
         * XEM CHI TIẾT QUIZ
         *
         * Response: QuizResponse (KHÔNG có isCorrect)
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> getQuizById(
                        @PathVariable String id) {

                log.info("REST request to get quiz by ID: {}", id);

                QuizResponse response = quizService.getQuizById(id);

                return ResponseEntity.ok(ApiResponse.<QuizResponse>builder()
                                .success(true)
                                .message("Quiz retrieved successfully")
                                .data(response)
                                .build());
        }

        /**
         * TÌM QUIZ THEO TOPIC
         */
        @GetMapping("/topic/{topic}")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByTopic(
                        @PathVariable String topic) {

                log.info("REST request to get quizzes by topic: {}", topic);

                List<QuizResponse> response = quizService.getQuizzesByTopic(topic);

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        /**
         * TÌM QUIZ THEO LEVEL
         */
        @GetMapping("/level/{level}")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> getQuizzesByLevel(
                        @PathVariable Integer level) {

                log.info("REST request to get quizzes by level: {}", level);

                List<QuizResponse> response = quizService.getQuizzesByLevel(level);

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Quizzes retrieved successfully")
                                .data(response)
                                .build());
        }

        /**
         * TÌM KIẾM QUIZ
         */
        @GetMapping("/search")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<List<QuizResponse>>> searchQuizzes(
                        @RequestParam String topic) {

                log.info("REST request to search quizzes by topic: {}", topic);

                List<QuizResponse> response = quizService.searchQuizzesByTopic(topic);

                return ResponseEntity.ok(ApiResponse.<List<QuizResponse>>builder()
                                .success(true)
                                .message("Search completed successfully")
                                .data(response)
                                .build());
        }

        /**
         * BẮT ĐẦU LÀM BÀI QUIZ
         *
         * QUAN TRỌNG: Cần track user nào đang làm bài
         */
        @PostMapping("/{quizId}/start")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<StartQuizResponse>> startQuiz(
                        @PathVariable String quizId,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = 
                        (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication.getPrincipal();
                String userId = userDetails.getUser().getId().toString();
                
                log.info("User {} started quiz {}", userId, quizId);

                StartQuizResponse response = quizAttemptService.startQuiz(quizId, userId);

                return ResponseEntity.ok(ApiResponse.<StartQuizResponse>builder()
                                .success(true)
                                .message("Bắt đầu làm bài thành công!")
                                .data(response)
                                .build());
        }

        /**
         * NỘP BÀI QUIZ
         *
         * QUAN TRỌNG: Cần track user nào đang submit
         */
        @PostMapping("/{quizId}/submit")
        @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
        public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitQuiz(
                        @PathVariable String quizId,
                        @RequestBody SubmitQuizRequest request,
                        Authentication authentication) {

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = 
                        (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication.getPrincipal();
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

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = 
                        (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication.getPrincipal();
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

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = 
                        (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication.getPrincipal();
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

                vn.hcmute.edu.materialsservice.security.CustomUserDetails userDetails = 
                        (vn.hcmute.edu.materialsservice.security.CustomUserDetails) authentication.getPrincipal();
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
        @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
                        @Valid @RequestBody QuizRequest requestDTO) {

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
        @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
                        @PathVariable String id,
                        @Valid @RequestBody QuizRequest requestDTO) {

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
        @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
        public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable String id) {

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
        @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
        public ResponseEntity<ApiResponse<QuizEditResponse>> getQuizForEdit(
                        @PathVariable String id) {

                log.info("REST request to get quiz for editing by ID: {}", id);

                QuizEditResponse response = quizService.getQuizForEdit(id);

                return ResponseEntity.ok(ApiResponse.<QuizEditResponse>builder()
                                .success(true)
                                .message("Quiz retrieved for editing")
                                .data(response)
                                .build());
        }

        /**
         * TẠO QUIZ TỪ FILE (Upload)
         *
         * Chỉ SUPPORTER và ADMIN
         */
        @PostMapping("/generate-from-file")
        @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
        public ResponseEntity<ApiResponse<QuizResponse>> generateQuizFromFile(
                        @ModelAttribute GenerateQuizFromFileRequest request) throws IOException {

                log.info("REST request to generate quiz from file");

                QuizResponse quiz = quizService.generateQuizFromFile(request);

                return ResponseEntity.ok(ApiResponse.success(quiz));
        }

        // ==================== ADMIN ONLY ====================

        /**
         * THỐNG KÊ QUIZ (nếu cần)
         *
         * Chỉ ADMIN
         */
        @GetMapping("/admin/statistics")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Object>> getQuizStatistics() {
                log.info("Admin is getting quiz statistics");

                // TODO: Implement statistics
                Object stats = Map.of(
                                "totalQuizzes", quizService.getAllQuizzes(Pageable.unpaged()).getTotalElements(),
                                "message", "Statistics endpoint - to be implemented");

                return ResponseEntity.ok(ApiResponse.builder()
                                .success(true)
                                .message("Statistics retrieved successfully")
                                .data(stats)
                                .build());
        }
}
