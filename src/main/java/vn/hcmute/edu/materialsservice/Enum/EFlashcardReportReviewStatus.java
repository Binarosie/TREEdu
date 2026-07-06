package vn.hcmute.edu.materialsservice.Enum;

public enum EFlashcardReportReviewStatus {
    REPORT_PENDING, // Báo cáo chờ xem xét
    REPORT_REVIEWING, // Supporter đang xem xét
    REPORT_RESOLVED, // Đã xử lý
    REPORT_REJECTED, // Từ chối báo cáo
    REVIEW_PENDING, // Review chờ xử lý
    REVIEW_APPROVED, // Admin phê duyệt (không vi phạm)
    REVIEW_VIOLATION // Admin xác định vi phạm
}
