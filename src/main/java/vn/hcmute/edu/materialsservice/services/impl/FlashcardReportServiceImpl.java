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
import vn.hcmute.edu.materialsservice.models.*;
import vn.hcmute.edu.materialsservice.repository.FlashcardReportRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.ReportLimitRepository;
import vn.hcmute.edu.materialsservice.repository.UserRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iFlashcardReportService;
import vn.hcmute.edu.materialsservice.services.iFlashcardReviewService;
import vn.hcmute.edu.materialsservice.services.observer.NotificationCenter;

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
    private final UserRepository userRepository;

    private static final String ADMIN_CLASS = "vn.hcmute.edu.materialsservice.models.Admin";
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

        // Notify owner flashcard bị report
        log.debug("[NOTI] ACTION=FLASHCARD_REPORTED | TO=OWNER | receiverId={} | flashcardId={} | flashcardTitle='{}' | reportedBy={} | reason='{}'",
                flashcard.getCreatedBy(), flashcardId, flashcard.getTitle(), memberId, request.getReason());

        NotificationCenter.notifyObservers(NotificationEvent.builder()
                .receiverId(flashcard.getCreatedBy())
                .type("SYSTEM")
                .title("Flashcard của bạn bị báo cáo")
                .content("Flashcard \"" + flashcard.getTitle() + "\" vừa bị báo cáo với lý do: " + request.getReason())
                .build());

        // Broadcast tới tất cả Supporter
        List<String> supporterIds = userRepository.findByUserType("vn.hcmute.edu.materialsservice.models.Supporter")
                .stream().map(u -> u.getId().toString()).toList();

        if (!supporterIds.isEmpty()) {
            log.debug("[NOTI] ACTION=FLASHCARD_REPORTED | TO=SUPPORTERS | receiverIds={} | flashcardId={} | flashcardTitle='{}'",
                    supporterIds, flashcardId, flashcard.getTitle());
            NotificationCenter.notifyObservers(NotificationEvent.builder()
                    .receiverIds(supporterIds)
                    .type("SYSTEM")
                    .title("Có flashcard cần xem xét")
                    .content("Flashcard \"" + flashcard.getTitle() + "\" vừa bị báo cáo và cần được xem xét.")
                    .build());
        } else {
            log.warn("[NOTI] ACTION=FLASHCARD_REPORTED | TO=SUPPORTERS | SKIPPED - no supporters found in DB");
        }

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

        Flashcard flashcard = flashcardRepository.findById(report.getFlashcardId())
                .orElseThrow(() -> new IllegalArgumentException("Flashcard không tồn tại"));

        EFlashcardReportReviewStatus newStatus;
        try {
            newStatus = EFlashcardReportReviewStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái báo cáo không hợp lệ");
        }

        report.setStatus(newStatus);
        report.setResolvedAt(LocalDateTime.now());
        FlashcardReport updated = reportRepository.save(report);

        switch (newStatus) {

            case REPORT_REJECTED -> {
                log.debug("[NOTI] ACTION=REPORT_REJECTED | TO=OWNER | receiverId={} | flashcardId={} | flashcardTitle='{}'",
                        flashcard.getCreatedBy(), flashcard.getId(), flashcard.getTitle());
                NotificationCenter.notifyObservers(NotificationEvent.builder()
                        .receiverId(flashcard.getCreatedBy())
                        .type("SYSTEM")
                        .title("Flashcard của bạn không bị vi phạm")
                        .content("Flashcard \"" + flashcard.getTitle() + "\" đã được xem xét và không vi phạm quy định.")
                        .build());
            }

            case REPORT_RESOLVED -> {
                try {
                    String reason = "Báo cáo flashcard được supporter xác nhận: " + report.getReason();
                    CreateReviewRequest reviewRequest = CreateReviewRequest.builder()
                            .reason(reason)
                            .build();
                    reviewService.createReviewRequest(report.getFlashcardId(), reviewRequest, authentication);
                    log.info("Tự động tạo review request cho flashcard: {}", report.getFlashcardId());
                } catch (Exception e) {
                    log.error("Lỗi khi tạo review request tự động: {}", e.getMessage());
                }

                List<String> adminIds = userRepository.findByUserType(ADMIN_CLASS)
                        .stream().map(User::getId).toList();

                if (!adminIds.isEmpty()) {
                    log.debug("[NOTI] ACTION=REPORT_RESOLVED | TO=ADMINS | receiverIds={} | flashcardId={} | flashcardTitle='{}'",
                            adminIds, flashcard.getId(), flashcard.getTitle());
                    NotificationCenter.notifyObservers(NotificationEvent.builder()
                            .receiverIds(adminIds)
                            .type("SYSTEM")
                            .title("Có flashcard cần admin xử lý")
                            .content("Supporter xác nhận flashcard \"" + flashcard.getTitle() + "\" có vi phạm, cần admin xem xét.")
                            .build());
                } else {
                    log.warn("[NOTI] ACTION=REPORT_RESOLVED | TO=ADMINS | SKIPPED - no admins found");
                }
            }

            default -> log.warn("Unhandled status: {}", newStatus);
        }

        return mapToResponse(updated);
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
