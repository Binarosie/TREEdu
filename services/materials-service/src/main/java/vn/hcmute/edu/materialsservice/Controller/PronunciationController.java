package vn.hcmute.edu.materialsservice.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.PronunciationCheckRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.PronunciationCheckResponse;
import vn.hcmute.edu.materialsservice.Dto.response.TopicResponse;
import vn.hcmute.edu.materialsservice.Service.PronunciationService;

import java.util.List;

@RestController
@RequestMapping("/api/pronunciation-check")
@RequiredArgsConstructor
public class PronunciationController {

    private final PronunciationService service;
    @GetMapping("/topics")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        return ResponseEntity.ok(ApiResponse.success(service.getTopics()));
    }

    @GetMapping("/random-sentence")
    public ResponseEntity<ApiResponse<String>> getRandomSentence(@RequestParam String topic) {
        return ResponseEntity.ok(ApiResponse.success(service.getRandomSentence(topic)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PronunciationCheckResponse>> checkPronunciation(
            PronunciationCheckRequest request) {
        PronunciationCheckResponse response = service.checkAndSave(request);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra phát âm thành công", response));
    }

}
