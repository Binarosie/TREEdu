package vn.hcmute.edu.materialsservice.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.Dto.response.TopicResponse;
import vn.hcmute.edu.materialsservice.Service.PronunciationService;
import vn.hcmute.edu.materialsservice.security.CustomUserDetails;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pronunciation-check")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')") // ✅ Áp dụng cho TẤT CẢ endpoints
public class PronunciationController {

    private final PronunciationService service;

    /**
     * Lấy danh sách tất cả topics
     */
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics(Authentication authentication) {
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        log.info("User {} fetching pronunciation topics", userDetails.getUser().getId());

        List<TopicResponse> topics = service.getTopics();
        return ResponseEntity.ok(ApiResponse.success(topics));
    }

    /**
     * Lấy câu ngẫu nhiên từ topic
     */
    @GetMapping("/random-sentence")
    public ResponseEntity<ApiResponse<String>> getRandomSentence(
            @RequestParam String topic,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        log.info("🎲 User {} getting random sentence from topic: {}",
                userDetails.getUser().getId(), topic);

        String sentence = service.getRandomSentence(topic);
        return ResponseEntity.ok(ApiResponse.success(sentence));
    }

    /**
     * Kiểm tra phát âm và lưu lịch sử
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PronunciationCheckResponse>> checkPronunciation(
            PronunciationCheckRequest request,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        log.info("🎤 User {} checking pronunciation for text: {}",
                userDetails.getUser().getId(), request.getExpectedText());

        PronunciationCheckResponse response = service.checkAndSave(request);

        log.info(" Pronunciation check completed with score: {}", response.getPronunciationScore());

        return ResponseEntity.ok(ApiResponse.success("Kiểm tra phát âm thành công", response));
    }
}