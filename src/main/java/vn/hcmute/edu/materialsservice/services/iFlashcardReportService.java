package vn.hcmute.edu.materialsservice.services;

import org.springframework.security.core.Authentication;
import vn.hcmute.edu.materialsservice.dtos.request.ReportFlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReportResponse;

import java.util.List;

public interface iFlashcardReportService {

    FlashcardReportResponse reportFlashcard(String flashcardId, ReportFlashcardRequest request,
            Authentication authentication);

    List<FlashcardReportResponse> getPendingReports(Authentication authentication);

    List<FlashcardReportResponse> getFlashcardReports(String flashcardId, Authentication authentication);

    FlashcardReportResponse updateReportStatus(String reportId, String status, Authentication authentication);

    Integer checkReportsRemaining(Authentication authentication);
}
