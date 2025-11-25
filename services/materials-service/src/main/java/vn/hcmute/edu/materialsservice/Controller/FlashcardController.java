package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.Service.FlashcardService;

import java.util.List;

@RestController
@RequestMapping("/flashcards")
@RequiredArgsConstructor
@Slf4j
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> createFlashcard(
            @Valid @RequestBody FlashcardRequest request) {
        FlashcardResponse response = flashcardService.createFlashcard(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo flashcard thành công", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> updateFlashcard(
            @PathVariable String id,
            @Valid @RequestBody FlashcardRequest request) {
        FlashcardResponse response = flashcardService.updateFlashcard(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật flashcard thành công", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashcard(@PathVariable String id) {
        flashcardService.deleteFlashcard(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa flashcard thành công", null));
    }

    /**
     * Lấy chi tiết flashcard (chỉ info, không có words)
     * GET /api/flashcards/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getFlashcardById(
            @PathVariable String id) {
        FlashcardResponse response = flashcardService.getFlashcardById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<FlashcardWithWordsResponse>> getFlashcardWithWords(
            @PathVariable String id) {
        FlashcardWithWordsResponse response = flashcardService.getFlashcardWithWords(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getAllFlashcards() {
        List<FlashcardResponse> responses = flashcardService.getAllFlashcard();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcardsByLevel(
            @PathVariable Integer level) {
        List<FlashcardResponse> responses = flashcardService.getFlashcardsByLevel(level);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcardsByTopic(
            @PathVariable String topic) {
        List<FlashcardResponse> responses = flashcardService.getFlashcardsByTopic(topic);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
