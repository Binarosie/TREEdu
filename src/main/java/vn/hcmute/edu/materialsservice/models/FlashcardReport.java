package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.time.LocalDateTime;

@Document(collection = "flashcard_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardReport {
    @Id
    private String id;

    private String flashcardId;
    private String reportedBy; // Member ID (người report)
    private String reason; // Nội dung báo cáo
    private EFlashcardReportReviewStatus status; // Trạng thái báo cáo

    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}
