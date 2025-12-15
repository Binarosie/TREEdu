package vn.hcmute.edu.materialsservice.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<WordResponse>> addWord(
            @PathVariable String flashcardId,
            @Valid @RequestBody WordRequest request,
            Authentication authentication) {

        WordResponse response = wordService.addWord(flashcardId, request, authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm từ thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WordResponse>>> getWordsByFlashcardId(
            @PathVariable String flashcardId,
            Authentication authentication) {

        System.out.println("=== CONTROLLER GET WORDS ===");
        System.out.println("FlashcardId: " + flashcardId);
        System.out.println("Authentication in controller: " + authentication);
        System.out.println("============================");

        List<WordResponse> responses = wordService.getWordsByFlashcardId(flashcardId, authentication);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WordResponse>> updateWord(
            @PathVariable String flashcardId,
            @PathVariable String id,
            @Valid @RequestBody WordRequest request,
            Authentication authentication) {

        WordResponse response = wordService.updateWord(id, request, authentication);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật từ thành công", response));
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable String flashcardId,
            @PathVariable String id,
            Authentication authentication) {

        wordService.deleteWord(id, authentication);

        return ResponseEntity.ok(ApiResponse.success("Xóa từ thành công", null));
    }
}
