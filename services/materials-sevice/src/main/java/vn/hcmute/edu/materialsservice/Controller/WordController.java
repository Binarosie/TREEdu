package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.WordRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.WordResponse;
import vn.hcmute.edu.materialsservice.Service.WordService;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards/{flashcardId}/words")
@RequiredArgsConstructor
@Slf4j
public class WordController {

    private final WordService wordService;

    /**
     * Thêm word vào flashcard
     * POST /api/flashcards/{flashcardId}/words
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WordResponse>> addWord(
            @PathVariable String flashcardId,
            @Valid @RequestBody WordRequest request) {

        WordResponse response = wordService.addWord(flashcardId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm từ thành công", response));
    }

    /**
     * Lấy tất cả words của một flashcard
     * GET /api/flashcards/{flashcardId}/words
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<WordResponse>>> getWordsByFlashcardId(
            @PathVariable String flashcardId) {

        List<WordResponse> responses = wordService.getWordsByFlashcardId(flashcardId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Cập nhật word
     * PUT /api/words/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> updateWord(
            @PathVariable String flashcardId,
            @PathVariable String id,
            @Valid @RequestBody WordRequest request) {

        WordResponse response = wordService.updateWord(id, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật từ thành công", response));
    }

    /**
     * Xóa word
     * DELETE /api/words/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable String flashcardId,
            @PathVariable String id) {

        wordService.deleteWord(id);

        return ResponseEntity.ok(ApiResponse.success("Xóa từ thành công", null));
    }
}
