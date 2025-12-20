package vn.hcmute.edu.materialsservice.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.Dto.request.FlashcardRequest;
import vn.hcmute.edu.materialsservice.Dto.response.ApiResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardResponse;
import vn.hcmute.edu.materialsservice.Dto.response.FlashcardWithWordsResponse;
import vn.hcmute.edu.materialsservice.Service.FlashcardService;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Slf4j
public class FlashcardController {

    private final FlashcardService flashcardService;

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<FlashcardResponse>> createFlashcard(
            @Valid @RequestBody FlashcardRequest request,
            Authentication authentication) {
        FlashcardResponse response = flashcardService.createFlashcard(request, authentication);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo flashcard thành công", response));
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> updateFlashcard(
            @PathVariable String id,
            @Valid @RequestBody FlashcardRequest request,
            Authentication authentication) {
        FlashcardResponse response = flashcardService.updateFlashcard(id, request, authentication);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật flashcard thành công", response));
    }

    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlashcard(
            @PathVariable String id,
            Authentication authentication) {
        flashcardService.deleteFlashcard(id, authentication);
        return ResponseEntity.ok(ApiResponse.success("Xóa flashcard thành công", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getFlashcardById(
            @PathVariable String id,
            Authentication authentication) {
        FlashcardResponse response = flashcardService.getFlashcardById(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<FlashcardWithWordsResponse>> getFlashcardWithWords(
            @PathVariable String id,
            Authentication authentication) {
        FlashcardWithWordsResponse response = flashcardService.getFlashcardWithWords(id, authentication);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getAllFlashcards(
            Authentication authentication,
            HttpServletRequest request) {

        System.out.println("=== DEBUG REQUEST ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + request.getHeader("Authorization"));

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                System.out.println("Cookie: " + c.getName() + "=" + c.getValue());
            }
        } else {
            System.out.println("No cookies");
        }

        System.out.println("Authentication: " + authentication);
        System.out.println("=====================");

        List<FlashcardResponse> responses = flashcardService.getAllFlashcard(authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcardsByLevel(
            @PathVariable Integer level,
            Authentication authentication) {
        List<FlashcardResponse> responses = flashcardService.getFlashcardsByLevel(level, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/topic/{topic}")
    public ResponseEntity<ApiResponse<List<FlashcardResponse>>> getFlashcardsByTopic(
            @PathVariable String topic,
            Authentication authentication) {
        List<FlashcardResponse> responses = flashcardService.getFlashcardsByTopic(topic, authentication);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
