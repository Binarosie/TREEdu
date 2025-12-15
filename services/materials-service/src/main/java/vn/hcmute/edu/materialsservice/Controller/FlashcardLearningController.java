package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.MarkWordViewedRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardProgressResponse;
import vn.hcmute.edu.materialsservice.Enum.LearningStatus;
import vn.hcmute.edu.materialsservice.Service.FlashcardLearningService;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards/learn")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
public class FlashcardLearningController {

    private final FlashcardLearningService learningService;

    /**
     * Bắt đầu học hoặc tiếp tục học flashcard
     * POST /api/flashcards/learn/{flashcardId}/start
     */
    @PostMapping("/{flashcardId}/start")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> startLearning(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.startOrContinueLearning(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Bắt đầu học flashcard thành công", response));
    }

    /**
     * Đánh dấu một word đã xem
     * PUT /api/flashcards/learn/{flashcardId}/mark-viewed
     */
    @PutMapping("/{flashcardId}/mark-viewed")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> markWordViewed(
            @PathVariable String flashcardId,
            @Valid @RequestBody MarkWordViewedRequest request,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.markWordAsViewed(
                flashcardId, request.getWordId(), authentication);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu word đã xem thành công", response));
    }

    /**
     * Lấy tiến trình học của một flashcard
     * GET /api/flashcards/learn/{flashcardId}
     */
    @GetMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> getLearningProgress(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.getLearningProgress(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Lấy tất cả flashcard đang học
     * GET /api/flashcards/learn
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardProgressResponse>>> getAllLearningProgress(
            Authentication authentication) {

        List<FlashcardProgressResponse> responses = learningService.getAllLearningProgress(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Lấy flashcard đang học theo trạng thái
     * GET /api/flashcards/learn/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<FlashcardProgressResponse>>> getLearningProgressByStatus(
            @PathVariable LearningStatus status,
            Authentication authentication) {

        List<FlashcardProgressResponse> responses = learningService.getLearningProgressByStatus(status, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Reset tiến trình học (học lại từ đầu)
     * POST /api/flashcards/learn/{flashcardId}/reset
     */
    @PostMapping("/{flashcardId}/reset")
    public ResponseEntity<ApiResponse<FlashcardProgressResponse>> resetProgress(
            @PathVariable String flashcardId,
            Authentication authentication) {

        FlashcardProgressResponse response = learningService.resetProgress(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success("Reset tiến trình học thành công", response));
    }
}
