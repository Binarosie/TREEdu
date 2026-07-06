package vn.hcmute.edu.materialsservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.hcmute.edu.materialsservice.models.FlashcardReport;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardReportRepository extends MongoRepository<FlashcardReport, String> {

    List<FlashcardReport> findByFlashcardId(String flashcardId);

    List<FlashcardReport> findByStatus(EFlashcardReportReviewStatus status);

    List<FlashcardReport> findByFlashcardIdAndStatus(String flashcardId, EFlashcardReportReviewStatus status);

    Optional<FlashcardReport> findByFlashcardIdAndReportedBy(String flashcardId, String reportedBy);

    List<FlashcardReport> findByStatusOrderByReportedAtDesc(EFlashcardReportReviewStatus status);
}
