package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.FlashcardReport;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardReportRepository extends MongoRepository<FlashcardReport, String> {

    /**
     * Tìm tất cả báo cáo cho một flashcard
     */
    List<FlashcardReport> findByFlashcardId(String flashcardId);

    /**
     * Tìm báo cáo theo trạng thái
     */
    List<FlashcardReport> findByStatus(EFlashcardReportReviewStatus status);

    /**
     * Tìm báo cáo của một flashcard theo trạng thái
     */
    List<FlashcardReport> findByFlashcardIdAndStatus(String flashcardId, EFlashcardReportReviewStatus status);

    /**
     * Kiểm tra xem member đã report flashcard này chưa
     */
    Optional<FlashcardReport> findByFlashcardIdAndReportedBy(String flashcardId, String reportedBy);

    /**
     * Lấy tất cả báo cáo chưa xử lý (PENDING)
     */
    List<FlashcardReport> findByStatusOrderByReportedAtDesc(EFlashcardReportReviewStatus status);
}
