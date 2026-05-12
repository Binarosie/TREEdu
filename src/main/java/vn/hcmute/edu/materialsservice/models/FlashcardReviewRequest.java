package vn.hcmute.edu.materialsservice.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "flashcard_review_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardReviewRequest {
    @Id
    private String id;

    private String flashcardId;
    private String supporterId; // Supporter tạo request
    private String reason; // Lý do gợi ý
    private List<String> reportIds; // Danh sách báo cáo liên quan

    private EFlashcardReportReviewStatus status; // Trạng thái review

    private String adminComment; // Ghi chú của admin

    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}
