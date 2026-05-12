package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.dtos.request.ReportFlashcardRequest;
import vn.hcmute.edu.materialsservice.dtos.request.CreateReviewRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReportResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;
import vn.hcmute.edu.materialsservice.Mapper.FlashcardMapper;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.models.FlashcardReport;
import vn.hcmute.edu.materialsservice.models.ReportLimit;
import vn.hcmute.edu.materialsservice.repository.FlashcardReportRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.ReportLimitRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iFlashcardReportService;
import vn.hcmute.edu.materialsservice.services.iFlashcardReviewService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardReportServiceImpl implements iFlashcardReportService {

    private final FlashcardReportRepository reportRepository;
    private final FlashcardRepository flashcardRepository;
    private final ReportLimitRepository reportLimitRepository;
    private final iFlashcardReviewService reviewService;

    private static final Integer DAILY_REPORT_LIMIT = 1;

    @Override
    @Transactional
    public FlashcardReportResponse reportFlashcard(
            String flashcardId,
            ReportFlashcardRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập để báo cáo flashcard");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUser().getId().toString();

        // Lấy flashcard
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard không tồn tại"));

        // Chỉ có thể báo cáo flashcard PUBLIC
        if (flashcard.getVisibility() != EFlashcardVisibility.PUBLIC) {
            throw new IllegalArgumentException("Chỉ có thể báo cáo flashcard ở chế độ PUBLIC");
        }

        // Không thể báo cáo flashcard của chính mình
        if (flashcard.getCreatedBy() != null && flashcard.getCreatedBy().equals(memberId)) {
            throw new IllegalArgumentException("Bạn không thể báo cáo flashcard do chính mình tạo");
        }

        // Kiểm tra member đã báo cáo flashcard này chưa
        Optional<FlashcardReport> existingReport = reportRepository.findByFlashcardIdAndReportedBy(flashcardId,
                memberId);
        if (existingReport.isPresent()) {
            throw new IllegalArgumentException("Bạn đã báo cáo flashcard này rồi");
        }

        // Kiểm tra lượt report còn lại
        if (!canReport(memberId)) {
            throw new IllegalStateException("Bạn đã hết lượt báo cáo trong hôm nay");
        }

        // Tạo báo cáo
        FlashcardReport report = FlashcardReport.builder()
                .flashcardId(flashcardId)
                .reportedBy(memberId)
                .reason(request.getReason())
                .status(EFlashcardReportReviewStatus.REPORT_PENDING)
                .reportedAt(LocalDateTime.now())
                .build();

        FlashcardReport savedReport = reportRepository.save(report);

        // Giảm lượt report
        decreaseReportsRemaining(memberId);

        log.info("Flashcard reported: {} by {}", flashcardId, memberId);

        return mapToResponse(savedReport);
    }

    @Override
    public List<FlashcardReportResponse> getPendingReports(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập để xem báo cáo");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        if (!isSupporter) {
            throw new AccessDeniedException("Chỉ supporter mới có quyền xem báo cáo");
        }

        List<FlashcardReport> reports = reportRepository
                .findByStatusOrderByReportedAtDesc(EFlashcardReportReviewStatus.REPORT_PENDING);
        return reports.stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<FlashcardReportResponse> getFlashcardReports(String flashcardId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập để xem báo cáo");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        if (!isSupporter) {
            throw new AccessDeniedException("Chỉ supporter mới có quyền xem báo cáo");
        }

        List<FlashcardReport> reports = reportRepository.findByFlashcardId(flashcardId);
        return reports.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public FlashcardReportResponse updateReportStatus(
            String reportId,
            String status,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        if (!isSupporter) {
            throw new AccessDeniedException("Chỉ supporter mới có quyền cập nhật trạng thái báo cáo");
        }

        FlashcardReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Báo cáo không tồn tại"));

        try {
            EFlashcardReportReviewStatus newStatus = EFlashcardReportReviewStatus.valueOf(status);
            report.setStatus(newStatus);
            report.setResolvedAt(LocalDateTime.now());

            FlashcardReport updated = reportRepository.save(report);

            if (newStatus == EFlashcardReportReviewStatus.REPORT_RESOLVED) {
                try {
                    String flashcardId = report.getFlashcardId();
                    String reason = "Báo cáo flashcard được supporter xác nhận: " + report.getReason();
                    CreateReviewRequest reviewRequest = CreateReviewRequest.builder()
                            .reason(reason)
                            .build();

                    reviewService.createReviewRequest(flashcardId, reviewRequest, authentication);
                    log.info("Tự động tạo review request cho flashcard: {} sau khi report resolved", flashcardId);
                } catch (Exception e) {
                    log.error("Lỗi khi tạo review request tự động: {}", e.getMessage());
                }
            }

            return mapToResponse(updated);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái báo cáo không hợp lệ");
        }
    }

    @Override
    public Integer checkReportsRemaining(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String memberId = userDetails.getUser().getId().toString();

        Optional<ReportLimit> limit = reportLimitRepository.findByMemberId(memberId);
        if (limit.isPresent()) {
            return limit.get().getReportsLeft();
        }

        // Nếu không có record, tạo mới với 1 lượt report
        createReportLimit(memberId);
        return 1;
    }

    private boolean canReport(String memberId) {
        Optional<ReportLimit> limit = reportLimitRepository.findByMemberId(memberId);

        if (limit.isEmpty()) {
            // Chưa có record, tạo mới
            createReportLimit(memberId);
            return true;
        }

        ReportLimit reportLimit = limit.get();
        LocalDate today = LocalDate.now();

        // Nếu hôm nay là ngày reset, reset lại
        if (reportLimit.getResetDate().isBefore(today)) {
            reportLimit.setReportsLeft(DAILY_REPORT_LIMIT);
            reportLimit.setResetDate(today.plusDays(1));
            reportLimitRepository.save(reportLimit);
            return true;
        }

        // Kiểm tra còn lượt report không
        return reportLimit.getReportsLeft() > 0;
    }

    private void decreaseReportsRemaining(String memberId) {
        Optional<ReportLimit> limit = reportLimitRepository.findByMemberId(memberId);

        if (limit.isEmpty()) {
            createReportLimit(memberId);
            return;
        }

        ReportLimit reportLimit = limit.get();
        reportLimit.setReportsLeft(reportLimit.getReportsLeft() - 1);
        reportLimitRepository.save(reportLimit);
    }

    private void createReportLimit(String memberId) {
        ReportLimit limit = ReportLimit.builder()
                .memberId(memberId)
                .reportsLeft(DAILY_REPORT_LIMIT - 1) // Trừ 1 vì đang report
                .resetDate(LocalDate.now().plusDays(1))
                .build();

        reportLimitRepository.save(limit);
    }

    private FlashcardReportResponse mapToResponse(FlashcardReport report) {
        return FlashcardReportResponse.builder()
                .id(report.getId())
                .flashcardId(report.getFlashcardId())
                .reportedBy(report.getReportedBy())
                .reason(report.getReason())
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
