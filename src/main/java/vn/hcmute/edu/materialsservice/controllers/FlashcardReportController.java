package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.ReportFlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReportResponse;
import vn.hcmute.edu.materialsservice.services.iFlashcardReportService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flashcard-reports")
@RequiredArgsConstructor
@Slf4j
public class FlashcardReportController {

    private final iFlashcardReportService reportService;

    // Member báo cáo flashcard
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardReportResponse>> reportFlashcard(
            @PathVariable String flashcardId,
            @Valid @RequestBody ReportFlashcardRequest request,
            Authentication authentication) {
        FlashcardReportResponse response = reportService.reportFlashcard(flashcardId, request, authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Báo cáo flashcard thành công", response));
    }

    // Supporter xem danh sách báo cáo chưa xử lý
    @PreAuthorize("hasAnyRole('ROLE_SUPPORTER')")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<FlashcardReportResponse>>> getPendingReports(
            Authentication authentication) {
        List<FlashcardReportResponse> responses = reportService.getPendingReports(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Supporter xem danh sách báo cáo của một flashcard
    @PreAuthorize("hasAnyRole('ROLE_SUPPORTER')")
    @GetMapping("/flashcard/{flashcardId}")
    public ResponseEntity<ApiResponse<List<FlashcardReportResponse>>> getFlashcardReports(
            @PathVariable String flashcardId,
            Authentication authentication) {
        List<FlashcardReportResponse> responses = reportService.getFlashcardReports(flashcardId, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    // Supporter cập nhật trạng thái báo cáo
    @PreAuthorize("hasAnyRole('ROLE_SUPPORTER')")
    @PutMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<FlashcardReportResponse>> updateReportStatus(
            @PathVariable String reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String status = request.get("status");
        FlashcardReportResponse response = reportService.updateReportStatus(reportId, status, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái báo cáo thành công", response));
    }

    // Member kiểm tra lượt báo cáo còn lại
    @PreAuthorize("hasAnyRole('ROLE_MEMBER')")
    @GetMapping("/remaining")
    public ResponseEntity<ApiResponse<Integer>> checkReportsRemaining(
            Authentication authentication) {
        Integer remaining = reportService.checkReportsRemaining(authentication);
        return ResponseEntity.ok(ApiResponse.success("Lượt báo cáo còn lại trong hôm nay", remaining));
    }
}
