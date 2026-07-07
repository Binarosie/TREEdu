package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.AppealRequest;
import vn.hcmute.edu.materialsservice.dtos.request.SendNotificationRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.NotificationDTO;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;
import vn.hcmute.edu.materialsservice.services.iNotificationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final iNotificationService notificationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications(
            Authentication authentication) {
        String userId = extractUserId(authentication);
        log.debug("[NOTIFICATION] GET /notifications | userId={} | userEmail={} | userRole={}",
                userId,
                ((CustomUserDetails) authentication.getPrincipal()).getUsername(),
                authentication.getAuthorities());
        List<NotificationDTO> notifications = notificationService.getMyNotifications(userId);
        log.debug("[NOTIFICATION] GET /notifications | userId={} | resultCount={}",
                userId, notifications.size());
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> countUnread(Authentication authentication) {
        String userId = extractUserId(authentication);
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu đã đọc", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        String userId = extractUserId(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Đã đánh dấu tất cả đã đọc", null));
    }

    private String extractUserId(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId().toString();
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/send-to-user")
    public ResponseEntity<ApiResponse<Void>> sendToUser(@RequestBody SendNotificationRequest request) {
        notificationService.sendToUser(request.getReceiverId(), request.getTitle(), request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi thông báo", null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/appeal")
    public ResponseEntity<ApiResponse<Void>> appeal(
            @RequestBody AppealRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        notificationService.sendAppealToAdmins(
                userDetails.getUser().getId(),
                userDetails.getUser().getEmail(),
                request.getContent());
        return ResponseEntity.ok(ApiResponse.success("Đã gửi phản hồi tới quản trị viên", null));
    }
}