package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.request.ReportFlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReportResponse;

import java.util.List;

public interface iFlashcardReportService {

    /**
     * Member báo cáo flashcard
     */
    FlashcardReportResponse reportFlashcard(String flashcardId, ReportFlashcardRequest request,
            Authentication authentication);

    /**
     * Lấy tất cả báo cáo chưa xử lý (cho Supporter)
     */
    List<FlashcardReportResponse> getPendingReports(Authentication authentication);

    /**
     * Lấy báo cáo của một flashcard
     */
    List<FlashcardReportResponse> getFlashcardReports(String flashcardId, Authentication authentication);

    /**
     * Cập nhật trạng thái báo cáo
     */
    FlashcardReportResponse updateReportStatus(String reportId, String status, Authentication authentication);

    /**
     * Kiểm tra member còn lượt report không
     */
    Integer checkReportsRemaining(Authentication authentication);
}
