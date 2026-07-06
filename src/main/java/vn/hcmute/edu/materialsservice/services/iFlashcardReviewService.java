package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.request.CreateReviewRequest;
import vn.hcmute.edu.materialsservice.dtos.request.ReviewDecisionRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReviewRequestResponse;

import java.util.List;

public interface iFlashcardReviewService {

        FlashcardReviewRequestResponse createReviewRequest(String flashcardId, CreateReviewRequest request,
                        Authentication authentication);

        List<FlashcardReviewRequestResponse> getPendingReviewRequests(Authentication authentication);

        FlashcardReviewRequestResponse reviewFlashcard(String reviewRequestId, ReviewDecisionRequest request,
                        Authentication authentication);

        FlashcardReviewRequestResponse getFlashcardReviewRequest(String flashcardId, Authentication authentication);
}
