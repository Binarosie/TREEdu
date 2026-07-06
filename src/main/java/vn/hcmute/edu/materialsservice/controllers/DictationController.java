package vn.hcmute.edu.materialsservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.hcmute.edu.materialsservice.Enum.ELessonStatus;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCheckRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationCreateRequest;
import vn.hcmute.edu.materialsservice.dtos.request.DictationUpdateRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.DictationCheckResponse;
import vn.hcmute.edu.materialsservice.models.DictationLesson;
import vn.hcmute.edu.materialsservice.services.CloudinaryService;
import vn.hcmute.edu.materialsservice.services.iDictationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictation")
@RequiredArgsConstructor
@Slf4j
public class DictationController {

    private final iDictationService dictationService;

    private final CloudinaryService cloudinaryService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostMapping
    public ResponseEntity<ApiResponse<DictationLesson>> createLesson(
            @Valid @RequestBody DictationCreateRequest request) {

        log.info("=== Supporter đang tạo bài nghe mới: {} ===", request.getTitle());
        DictationLesson response = dictationService.createLesson(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bài nghe chính tả thành công!", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DictationLesson>> getLessonById(@PathVariable String id) {
        log.info("=== Lấy chi tiết bài nghe ID: {} ===", id);
        DictationLesson response = dictationService.getLessonById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{dictationId}/check")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DictationCheckResponse>> checkAnswer(
            @PathVariable String dictationId,
            @Valid @RequestBody DictationCheckRequest request) {

        log.info("=== Check chính tả bài tập ID: {} - Đoạn số: {} ===", dictationId, request.getSegmentId());
        DictationCheckResponse response = dictationService.checkAnswer(dictationId, request);

        return ResponseEntity.ok(ApiResponse.success("Kiểm tra đáp án thành công", response));
    }

    @PostMapping("/generate-by-ai")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DictationLesson>> generateLessonByAI(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("level") String level) {

        log.info("=== Bắt đầu xử lý file [{}] bằng AI ===", file.getOriginalFilename());

        if (file.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "File không được để trống!"));
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("audio/") && !contentType.equals("video/mp4"))) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "Chỉ chấp nhận file audio hoặc mp4!"));
        }

        try {
            // Upload thẳng lên Cloudinary, không cần lưu disk nữa
            String audioUrl = cloudinaryService.uploadAudio(file);
            log.info("Cloudinary URL: {}", audioUrl);

            // Gửi file gốc sang Python AI để bóc băng (file vẫn còn trong memory)
            DictationLesson response = dictationService.generateLessonWithAI(file, title, level, audioUrl);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success("AI đã tạo bài nghe thành công!", response));

        } catch (IOException e) {
            log.error("Lỗi upload Cloudinary: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Không thể upload file: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<DictationLesson>>> getAllLessonsForAdmin() {
        log.info("=== Supporter lấy toàn bộ danh sách bài nghe ===");
        List<DictationLesson> responses = dictationService.getAllLessonsForAdmin();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable String id,
            @RequestParam("status") ELessonStatus status) {

        log.info("=== Cập nhật trạng thái bài nghe ID: {} sang [{}] ===", id, status);
        dictationService.updateLessonStatus(id, status);

        return ResponseEntity
                .ok(ApiResponse.success("Cập nhật trạng thái bài học thành công!", "Trạng thái mới: " + status));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DictationLesson>>> getAllLessonsForMember() {
        log.info("=== Member lấy danh sách bài nghe công khai ===");
        List<DictationLesson> responses = dictationService.getAllLessonsForMember();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER','ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<DictationLesson>> updateLesson(
            @PathVariable String id,
            @RequestBody DictationUpdateRequest request) {

        log.info("=== Supporter đang chỉnh sửa chi tiết bài nghe ID: {} ===", id);
        DictationLesson updatedLesson = dictationService.updateLesson(id, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật chi tiết bài học thành công!", updatedLesson));
    }
}
