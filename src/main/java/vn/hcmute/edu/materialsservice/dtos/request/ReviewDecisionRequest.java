package vn.hcmute.edu.materialsservice.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDecisionRequest {

    @NotNull(message = "Trạng thái review không được để trống")
    private EFlashcardReportReviewStatus status; // REVIEW_APPROVED hoặc REVIEW_VIOLATION

    private String adminComment;
}
