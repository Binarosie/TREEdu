package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.CreateReviewRequest;
import vn.hcmute.edu.materialsservice.dtos.request.ReviewDecisionRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReviewRequestResponse;
import vn.hcmute.edu.materialsservice.services.iFlashcardReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard-reviews")
@RequiredArgsConstructor
@Slf4j
public class FlashcardReviewController {

    private final iFlashcardReviewService reviewService;

    /**
     * Supporter tạo yêu cầu review cho Flashcard
     */
    @PreAuthorize("hasRole('SUPPORTER')")
    @PostMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardReviewRequestResponse>> createReviewRequest(
            @PathVariable String flashcardId,
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {
        FlashcardReviewRequestResponse response = reviewService.createReviewRequest(flashcardId, request,
                authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu review thành công", response));
    }

    /**
     * Admin xem danh sách yêu cầu review chờ xử lý
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FlashcardReviewRequestResponse>>> getPendingReviewRequests(
            Authentication authentication) {
        List<FlashcardReviewRequestResponse> responses = reviewService.getPendingReviewRequests(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Admin phê duyệt hoặc từ chối yêu cầu review
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{reviewRequestId}/decision")
    public ResponseEntity<ApiResponse<FlashcardReviewRequestResponse>> reviewFlashcard(
            @PathVariable String reviewRequestId,
            @Valid @RequestBody ReviewDecisionRequest request,
            Authentication authentication) {
        FlashcardReviewRequestResponse response = reviewService.reviewFlashcard(reviewRequestId, request,
                authentication);
        return ResponseEntity.ok(ApiResponse.success("Xử lý yêu cầu review thành công", response));
    }

    /**
     * Supporter/Admin xem chi tiết review request của một flashcard
     */
    @PreAuthorize("hasAnyRole('SUPPORTER', 'ADMIN')")
    @GetMapping("/flashcard/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardReviewRequestResponse>> getFlashcardReviewRequest(
            @PathVariable String flashcardId,
            Authentication authentication) {
        FlashcardReviewRequestResponse response = reviewService.getFlashcardReviewRequest(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
