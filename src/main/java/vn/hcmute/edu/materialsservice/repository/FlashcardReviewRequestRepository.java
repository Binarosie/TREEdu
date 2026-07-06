package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.FlashcardReviewRequest;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardReviewRequestRepository extends MongoRepository<FlashcardReviewRequest, String> {

    Optional<FlashcardReviewRequest> findByFlashcardIdAndStatus(
            String flashcardId,
            EFlashcardReportReviewStatus status);

    List<FlashcardReviewRequest> findAllByFlashcardId(String flashcardId);

    List<FlashcardReviewRequest> findByStatus(EFlashcardReportReviewStatus status);

    List<FlashcardReviewRequest> findByStatusOrderByRequestedAtDesc(EFlashcardReportReviewStatus status);

    Optional<FlashcardReviewRequest> findByFlashcardIdAndSupporterId(String flashcardId, String supporterId);
}
