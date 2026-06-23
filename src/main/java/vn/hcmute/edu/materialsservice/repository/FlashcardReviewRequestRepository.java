package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.FlashcardReviewRequest;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardReviewRequestRepository extends MongoRepository<FlashcardReviewRequest, String> {

    /**
     * Tìm review request theo flashcard ID
     */
    Optional<FlashcardReviewRequest> findByFlashcardIdAndStatus(
            String flashcardId,
            EFlashcardReportReviewStatus status
    );

    List<FlashcardReviewRequest> findAllByFlashcardId(String flashcardId);

    /**
     * Tìm tất cả review request theo trạng thái
     */
    List<FlashcardReviewRequest> findByStatus(EFlashcardReportReviewStatus status);

    /**
     * Lấy tất cả review request chờ xử lý (PENDING)
     */
    List<FlashcardReviewRequest> findByStatusOrderByRequestedAtDesc(EFlashcardReportReviewStatus status);

    /**
     * Kiểm tra supporter đã tạo request cho flashcard này chưa
     */
    Optional<FlashcardReviewRequest> findByFlashcardIdAndSupporterId(String flashcardId, String supporterId);
}
