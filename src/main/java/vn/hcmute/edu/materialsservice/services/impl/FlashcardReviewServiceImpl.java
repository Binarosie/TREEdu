package vn.hcmute.edu.materialsservice.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.materialsservice.dtos.request.CreateReviewRequest;
import vn.hcmute.edu.materialsservice.dtos.request.ReviewDecisionRequest;
import vn.hcmute.edu.materialsservice.dtos.response.FlashcardReviewRequestResponse;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardReportReviewStatus;
import vn.hcmute.edu.materialsservice.Enum.EFlashcardVisibility;
import vn.hcmute.edu.materialsservice.models.Flashcard;
import vn.hcmute.edu.materialsservice.models.FlashcardReport;
import vn.hcmute.edu.materialsservice.models.FlashcardReviewRequest;
import vn.hcmute.edu.materialsservice.models.NotificationEvent;
import vn.hcmute.edu.materialsservice.repository.FlashcardReportRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardReviewRequestRepository;
import vn.hcmute.edu.materialsservice.repository.NotificationRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iFlashcardReviewService;
import vn.hcmute.edu.materialsservice.services.observer.NotificationCenter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardReviewServiceImpl implements iFlashcardReviewService {

    private final FlashcardReviewRequestRepository reviewRepository;
    private final FlashcardReportRepository reportRepository;
    private final FlashcardRepository flashcardRepository;
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public FlashcardReviewRequestResponse createReviewRequest(
            String flashcardId,
            CreateReviewRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập để tạo review request");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER"));

        if (!isSupporter) {
            throw new AccessDeniedException("Chỉ supporter mới có quyền tạo review request");
        }

        String supporterId = userDetails.getUser().getId().toString();

        // Lấy flashcard
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Flashcard không tồn tại"));

        // Kiểm tra xem đã tồn tại review request PENDING chưa
        Optional<FlashcardReviewRequest> existingReview = reviewRepository
                .findByFlashcardIdAndStatus(flashcardId, EFlashcardReportReviewStatus.REVIEW_PENDING);
        if (existingReview.isPresent()
                && existingReview.get().getStatus() == EFlashcardReportReviewStatus.REVIEW_PENDING) {
            throw new IllegalArgumentException("Đã có review request PENDING cho flashcard này");
        }

        // Lấy tất cả báo cáo của flashcard (không phụ thuộc vào status)
        // Vì có thể được gọi từ updateReportStatus sau khi report đã RESOLVED
        List<FlashcardReport> allReports = reportRepository.findByFlashcardId(flashcardId);

        if (allReports.isEmpty()) {
            throw new IllegalArgumentException("Không có báo cáo nào cho flashcard này");
        }

        // Tạo review request
        FlashcardReviewRequest reviewRequest = FlashcardReviewRequest.builder()
                .flashcardId(flashcardId)
                .supporterId(supporterId)
                .reason(request.getReason())
                .reportIds(allReports.stream().map(FlashcardReport::getId).toList())
                .status(EFlashcardReportReviewStatus.REVIEW_PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        FlashcardReviewRequest saved = reviewRepository.save(reviewRequest);

        // Cập nhật trạng thái báo cáo thành REVIEWING (nếu chưa phải RESOLVED)
        allReports.forEach(report -> {
            if (report.getStatus() != EFlashcardReportReviewStatus.REPORT_RESOLVED) {
                report.setStatus(EFlashcardReportReviewStatus.REPORT_REVIEWING);
                reportRepository.save(report);
            }
        });

        log.info("Review request created for flashcard {} by supporter {}. Total reports: {}",
                flashcardId, supporterId, allReports.size());

        log.debug("[REVIEW] ACTION=CREATE_REVIEW_REQUEST | supporterId={} | flashcardId={} | flashcardTitle='{}' | reportIds={} | reason='{}'",
                supporterId, flashcardId, flashcard.getTitle(),
                allReports.stream().map(FlashcardReport::getId).toList(),
                request.getReason());

        return mapToResponse(saved);
    }

    @Override
    public List<FlashcardReviewRequestResponse> getPendingReviewRequests(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Chỉ admin mới có quyền xem review request");
        }

        List<FlashcardReviewRequest> reviews = reviewRepository
                .findByStatusOrderByRequestedAtDesc(EFlashcardReportReviewStatus.REVIEW_PENDING);
        return reviews.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public FlashcardReviewRequestResponse reviewFlashcard(
            String reviewRequestId,
            ReviewDecisionRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Chỉ admin mới có quyền xử lý review request");
        }

        FlashcardReviewRequest reviewRequest = reviewRepository.findById(reviewRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Review request không tồn tại"));

        // Lấy flashcard TRƯỚC — khai báo ở đầu method, không nằm trong if block
        Flashcard flashcard = flashcardRepository.findById(reviewRequest.getFlashcardId())
                .orElseThrow(() -> new IllegalArgumentException("Flashcard không tồn tại"));

        // Lấy reporterId TRƯỚC
        String reporterId = null;
        if (reviewRequest.getReportIds() != null && !reviewRequest.getReportIds().isEmpty()) {
            FlashcardReport firstReport = reportRepository.findById(reviewRequest.getReportIds().get(0))
                    .orElse(null);
            if (firstReport != null) {
                reporterId = firstReport.getReportedBy();
            }
        }

        // Ngay sau khi lấy được flashcard và reporterId, trước switch
        String adminId = userDetails.getUser().getId().toString();
        log.debug("[REVIEW] ACTION=PROCESS_REVIEW | adminId={} | reviewRequestId={} | flashcardId={} | flashcardTitle='{}' | decision={} | reporterId={} | adminComment='{}'",
                adminId, reviewRequestId, flashcard.getId(), flashcard.getTitle(),
                request.getStatus(), reporterId, request.getAdminComment());

        // Cập nhật trạng thái review request
        reviewRequest.setStatus(request.getStatus());
        reviewRequest.setAdminComment(request.getAdminComment());
        reviewRequest.setReviewedAt(LocalDateTime.now());

        // Nếu là VIOLATION, đánh dấu flashcard vi phạm
        if (request.getStatus() == EFlashcardReportReviewStatus.REVIEW_VIOLATION) {
            flashcard = flashcardRepository.findById(reviewRequest.getFlashcardId())
                    .orElseThrow(() -> new IllegalArgumentException("Flashcard không tồn tại"));

            flashcard.setIsViolated(true);
            flashcard.setVisibility(EFlashcardVisibility.PRIVATE); // Chuyển về PRIVATE
            flashcardRepository.save(flashcard);

            log.info("Flashcard {} marked as violated", flashcard.getId());
        }

        // Cập nhật trạng thái báo cáo thành RESOLVED
        if (reviewRequest.getReportIds() != null) {
            reviewRequest.getReportIds().forEach(reportId -> {
                FlashcardReport report = reportRepository.findById(reportId)
                        .orElse(null);
                if (report != null) {
                    report.setStatus(EFlashcardReportReviewStatus.REPORT_RESOLVED);
                    reportRepository.save(report);
                }
            });
        }

        FlashcardReviewRequest updated = reviewRepository.save(reviewRequest);

        switch (request.getStatus()) {

            case REVIEW_APPROVED -> {
                log.debug("[NOTI] ACTION=REVIEW_APPROVED | TO=OWNER | receiverId={} | flashcardId={} | flashcardTitle='{}' | reviewRequestId={} | adminComment='{}'",
                        flashcard.getCreatedBy(), flashcard.getId(), flashcard.getTitle(), reviewRequestId, request.getAdminComment());

                // Admin: không vi phạm → notify owner
                NotificationCenter.notifyObservers(NotificationEvent.builder()
                        .receiverId(flashcard.getCreatedBy())
                        .type("SYSTEM")
                        .title("Flashcard của bạn không bị vi phạm")
                        .content("Admin đã xem xét và xác nhận flashcard \""
                                + flashcard.getTitle() + "\" không vi phạm quy định.")
                        .build());
            }

            case REVIEW_VIOLATION -> {
                log.debug("[NOTI] ACTION=REVIEW_VIOLATION | TO=OWNER | receiverId={} | flashcardId={} | flashcardTitle='{}' | reviewRequestId={} | adminComment='{}'",
                        flashcard.getCreatedBy(), flashcard.getId(), flashcard.getTitle(), reviewRequestId, request.getAdminComment());

                // Notify owner: bị khóa
                NotificationCenter.notifyObservers(NotificationEvent.builder()
                        .receiverId(flashcard.getCreatedBy())
                        .type("SYSTEM")
                        .title("Flashcard của bạn đã bị khóa")
                        .content("Flashcard \"" + flashcard.getTitle()
                                + "\" đã bị khóa do vi phạm quy định cộng đồng.")
                        .build());

                // Notify reporter: đã xử lý xong
                if (reporterId != null) {
                    log.debug("[NOTI] ACTION=REVIEW_VIOLATION | TO=REPORTER | receiverId={} | flashcardId={} | flashcardTitle='{}' | reviewRequestId={}",
                            reporterId, flashcard.getId(), flashcard.getTitle(), reviewRequestId);
                    NotificationCenter.notifyObservers(NotificationEvent.builder()
                            .receiverId(reporterId)
                            .type("SYSTEM")
                            .title("Báo cáo của bạn đã được xử lý")
                            .content("Báo cáo của bạn về flashcard \""
                                    + flashcard.getTitle()
                                    + "\" đã được xử lý. Cảm ơn bạn đã đóng góp.")
                            .build());
                } else {
                    log.warn("[NOTI] ACTION=REVIEW_VIOLATION | TO=REPORTER | SKIPPED - reporterId is null | reviewRequestId={} | flashcardId={}",
                            reviewRequestId, flashcard.getId());
                }
            }

            default -> log.warn("Unhandled review status: {}", request.getStatus());
        }

        log.info("Review request {} processed by admin with status {}", reviewRequestId, request.getStatus());

        return mapToResponse(updated);
    }

    @Override
    public FlashcardReviewRequestResponse getFlashcardReviewRequest(String flashcardId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Bạn phải đăng nhập");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isSupporter = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPORTER") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isSupporter) {
            throw new AccessDeniedException("Chỉ supporter và admin mới có quyền xem review request");
        }

        FlashcardReviewRequest reviewRequest = reviewRepository
                .findByFlashcardIdAndStatus(flashcardId, EFlashcardReportReviewStatus.REVIEW_PENDING)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy review request cho flashcard này"));

        return mapToResponse(reviewRequest);
    }

    private FlashcardReviewRequestResponse mapToResponse(FlashcardReviewRequest review) {
        return FlashcardReviewRequestResponse.builder()
                .id(review.getId())
                .flashcardId(review.getFlashcardId())
                .supporterId(review.getSupporterId())
                .reason(review.getReason())
                .reportIds(review.getReportIds())
                .status(review.getStatus())
                .adminComment(review.getAdminComment())
                .requestedAt(review.getRequestedAt())
                .reviewedAt(review.getReviewedAt())
                .build();
    }
}
