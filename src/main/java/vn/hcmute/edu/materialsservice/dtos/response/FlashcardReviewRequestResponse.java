package vn.hcmute.edu.materialsservice.dtos.response;

import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardReviewRequestResponse {

    private String id;
    private String flashcardId;
    private String supporterId;
    private String reason;
    private List<String> reportIds;
    private EFlashcardReportReviewStatus status;
    private String adminComment;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}
