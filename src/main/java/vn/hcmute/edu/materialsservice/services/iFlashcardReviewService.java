package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.request.CreateReviewRequest;
import vn.hcmute.edu.materialsservice.dtos.request.ReviewDecisionRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReviewRequestResponse;

import java.util.List;

public interface iFlashcardReviewService {

    /**
     * Supporter tạo yêu cầu review cho Flashcard
     */
    FlashcardReviewRequestResponse createReviewRequest(String flashcardId, CreateReviewRequest request,
            Authentication authentication);

    /**
     * Lấy tất cả review request chờ xử lý (cho Admin)
     */
    List<FlashcardReviewRequestResponse> getPendingReviewRequests(Authentication authentication);

    /**
     * Admin phê duyệt hoặc từ chối review
     */
    FlashcardReviewRequestResponse reviewFlashcard(String reviewRequestId, ReviewDecisionRequest request,
            Authentication authentication);

    /**
     * Lấy review request của một flashcard
     */
    FlashcardReviewRequestResponse getFlashcardReviewRequest(String flashcardId, Authentication authentication);
}
