package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;
import vn.hcmute.edu.materialsservice.services.iPronunciationService;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pronunciation-check")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
public class PronunciationController {

    private final iPronunciationService service;

    // =========================================================================
    // Topic helpers (giữ lại để tương thích với client cũ)
    // =========================================================================

    /**
     * GET /api/pronunciation-check/topics
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics(
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User {} fetching topics", userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.success(service.getTopics()));
    }

    /**
     * GET /api/pronunciation-check/random-sentence?topic=...
     */
    @GetMapping("/random-sentence")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> getRandomSentence(
            @RequestParam String topic,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User {} getting random sentence, topic={}", userDetails.getUser().getId(), topic);
        return ResponseEntity.ok(ApiResponse.success(service.getRandomSentence(topic)));
    }

    // =========================================================================
    // Pronunciation History CRUD
    // =========================================================================

    /**
     * POST /api/pronunciation-check
     * Kiểm tra phát âm và lưu lịch sử.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PronunciationCheckResponse>> checkPronunciation(
            PronunciationCheckRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User {} checking pronunciation: {}", userDetails.getUser().getId(),
                request.getExpectedText());

        PronunciationCheckResponse response = service.checkAndSave(request);
        log.info("Pronunciation score={}", response.getPronunciationScore());
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra phát âm thành công", response));
    }

    /**
     * GET /api/pronunciation-check/history
     * Lấy toàn bộ lịch sử.
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<PronunciationCheckResponse>>> getAllHistory() {
        return ResponseEntity.ok(ApiResponse.success(service.getAll()));
    }

    /**
     * GET /api/pronunciation-check/history/{id}
     * Lấy 1 lịch sử theo id.
     */
    @GetMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PronunciationCheckResponse>> getHistoryById(
            @PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getById(id)));
    }

    /**
     * DELETE /api/pronunciation-check/history/{id}
     * Xóa 1 lịch sử. ADMIN hoặc chính chủ (hiện tại mở cho ADMIN).
     */
    @DeleteMapping("/history/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHistory(
            @PathVariable String id,
            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        log.info("User {} deleting history id={}", userDetails.getUser().getId(), id);
        service.deleteHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa lịch sử thành công", null));
    }
}
