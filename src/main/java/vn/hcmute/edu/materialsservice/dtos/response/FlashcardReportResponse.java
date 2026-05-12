package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardReportResponse {

    private String id;
    private String flashcardId;
    private String reportedBy; // Member ID
    private String reason;
    private EFlashcardReportReviewStatus status;
    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
}
