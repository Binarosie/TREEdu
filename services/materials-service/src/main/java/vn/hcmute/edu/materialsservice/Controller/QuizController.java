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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.GenerateQuizFromFileRequest;
import vn.hcmute.edu.materialsservice.Dto.request.QuizRequest;
import vn.hcmute.edu.materialsservice.Dto.request.SubmitQuizRequest;
import vn.hcmute.edu.materialsservice.Dto.response.*;
import vn.hcmute.edu.materialsservice.Service.QuizAttemptService;
import vn.hcmute.edu.materialsservice.Service.QuizService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;
    private final QuizAttemptService quizAttemptService;

    @PostMapping
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

    @GetMapping("/{id}")
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
    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuizResponse>>> getAllQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        log.info("REST request to get all quizzes - page: {}, size: {}", page, size);

        // Parse sort parameters
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


    @GetMapping("/topic/{topic}")
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


    @GetMapping("/level/{level}")
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


    @GetMapping("/search")
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


    @PutMapping("/{id}")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable String id) {

        log.info("REST request to delete quiz with ID: {}", id);

        quizService.deleteQuiz(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Quiz deleted successfully")
                .build());
    }
    @PostMapping("/{quizId}/start")
    public ResponseEntity<ApiResponse<StartQuizResponse>> startQuiz(@PathVariable String quizId) {
        StartQuizResponse response = quizAttemptService.startQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.<StartQuizResponse>builder()
                .success(true)
                .message("Bắt đầu làm bài thành công!")
                .data(response)
                .build());
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<ApiResponse<QuizAttemptResponse>> submitQuiz(
            @PathVariable String quizId,
            @RequestBody SubmitQuizRequest request) {

        QuizAttemptResponse response = quizAttemptService.submitQuiz(quizId, request);
        return ResponseEntity.ok(ApiResponse.<QuizAttemptResponse>builder()
                .success(true)
                .message("Nộp bài thành công!")
                .data(response)
                .build());
    }
    @PostMapping("/generate-from-file")
    public ResponseEntity<ApiResponse<QuizResponse>> generateQuizFromFile(
            @ModelAttribute GenerateQuizFromFileRequest request) throws IOException {

        QuizResponse quiz = quizService.generateQuizFromFile(request);
        return ResponseEntity.ok(ApiResponse.success(quiz));
    }

    @GetMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<QuizEditResponse>> getQuizForEdit(@PathVariable String id) {
        log.info("REST request to get quiz for editing by ID: {}", id);

        QuizEditResponse response = quizService.getQuizForEdit(id); // mới

        return ResponseEntity.ok(ApiResponse.<QuizEditResponse>builder()
                .success(true)
                .message("Quiz retrieved for editing")
                .data(response)
                .build());
    }
}
