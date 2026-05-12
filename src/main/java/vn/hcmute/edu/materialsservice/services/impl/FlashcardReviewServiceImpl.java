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
import vn.hcmute.edu.materialsservice.repository.FlashcardReportRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardRepository;
import vn.hcmute.edu.materialsservice.repository.FlashcardReviewRequestRepository;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iFlashcardReviewService;

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
        Optional<FlashcardReviewRequest> existingReview = reviewRepository.findByFlashcardId(flashcardId);
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

        // Cập nhật trạng thái review request
        reviewRequest.setStatus(request.getStatus());
        reviewRequest.setAdminComment(request.getAdminComment());
        reviewRequest.setReviewedAt(LocalDateTime.now());

        // Nếu là VIOLATION, đánh dấu flashcard vi phạm
        if (request.getStatus() == EFlashcardReportReviewStatus.REVIEW_VIOLATION) {
            Flashcard flashcard = flashcardRepository.findById(reviewRequest.getFlashcardId())
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

        FlashcardReviewRequest reviewRequest = reviewRepository.findByFlashcardId(flashcardId)
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
