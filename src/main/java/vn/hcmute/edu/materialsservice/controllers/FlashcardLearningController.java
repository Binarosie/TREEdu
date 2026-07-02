package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.MarkWordViewedRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SubmitWordAnswerRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.dtos.response.WordCheckResponse;
import vn.hcmute.edu.materialsservice.Enum.ELearningStatus;
import vn.hcmute.edu.materialsservice.services.iFlashcardLearningService;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards/learn")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
public class FlashcardLearningController {

    private final iFlashcardLearningService learningService;

    // Bắt đầu học hoặc tiếp tục học flashcard
    @PostMapping("/{flashcardId}/start")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> startLearning(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.startOrContinueLearning(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu học flashcard thành công", response));
    }

    // Đánh dấu một word đã xem
    @PutMapping("/{flashcardId}/mark-viewed")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> markWordViewed(
            @PathVariable String flashcardId,
            @Valid @RequestBody MarkWordViewedRequest request,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.markWordAsViewed(
                flashcardId, request.getWordId(), authentication);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu word đã xem thành công", response));
    }

    // Gõ và submit từ vựng, hệ thống check đúng/sai
    @PostMapping("/{flashcardId}/submit-answer")
    public ResponseEntity<ApiResponse<WordCheckResponse>> submitWordAnswer(
            @PathVariable String flashcardId,
            @Valid @RequestBody SubmitWordAnswerRequest request,
            Authentication authentication) {

        WordCheckResponse response = learningService.submitWordAnswer(
                flashcardId, request.getWordId(), request.getUserAnswer(), authentication);

        String message = response.isCorrect() ? "Chính xác!" : "Sai, vui lòng thử lại";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // Lấy tiến trình học của một flashcard
    @GetMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> getLearningProgress(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.getLearningProgress(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Lấy tất cả flashcard đang học
    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardProgressResponse>>> getAllLearningProgress(
            Authentication authentication) {

        List<FlashcardProgressResponse> responses = learningService.getAllLearningProgress(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Lấy flashcard đang học theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FlashcardProgressResponse>>> getLearningProgressByStatus(
            @PathVariable ELearningStatus status,
            Authentication authentication) {

        List<FlashcardProgressResponse> responses = learningService.getLearningProgressByStatus(status, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Reset tiến trình học (học lại từ đầu)
    @PostMapping("/{flashcardId}/reset")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> resetProgress(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.resetProgress(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Reset tiến trình học thành công", response));
    }
}
