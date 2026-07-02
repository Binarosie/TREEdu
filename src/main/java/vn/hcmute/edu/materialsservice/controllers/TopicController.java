package vn.hcmute.edu.materialsservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.materialsservice.dtos.request.SentencesRequest;
import vn.hcmute.edu.materialsservice.dtos.request.TopicRequest;
import vn.hcmute.edu.materialsservice.dtos.response.ApiResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicDetailResponse;
import vn.hcmute.edu.materialsservice.dtos.response.TopicResponse;
import vn.hcmute.edu.materialsservice.services.iPronunciationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
public class TopicController {

    private final iPronunciationService service;

    
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<TopicResponse>>> getAllTopics() {
        return ResponseEntity.ok(ApiResponse.success(service.getTopics()));
    }

    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_MEMBER', 'ROLE_SUPPORTER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TopicDetailResponse>> getTopicById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTopicById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TopicDetailResponse>> createTopic(
            @RequestBody TopicRequest request) {

        log.info("Admin creating topic: {}", request.getName());
        TopicDetailResponse created = service.createTopic(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo topic thành công", created));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TopicDetailResponse>> updateTopic(
            @PathVariable String id,
            @RequestBody TopicRequest request) {

        log.info("Admin updating topic id={}", id);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật topic thành công",
                service.updateTopic(id, request)));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTopic(@PathVariable String id) {
        log.info("Admin deleting topic id={}", id);
        service.deleteTopic(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa topic thành công", null));
    }

   
    @PostMapping("/{id}/sentences")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TopicDetailResponse>> addSentences(
            @PathVariable String id,
            @RequestBody SentencesRequest request) {

        log.info("Admin adding {} sentence(s) to topic id={}", request.getSentences().size(), id);
        return ResponseEntity.ok(ApiResponse.success("Thêm câu thành công",
                service.addSentences(id, request)));
    }

   
    @DeleteMapping("/{id}/sentences/{index}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<TopicDetailResponse>> removeSentence(
            @PathVariable String id,
            @PathVariable int index) {

        log.info("Admin removing sentence[{}] from topic id={}", index, id);
        return ResponseEntity.ok(ApiResponse.success("Xóa câu thành công",
                service.removeSentence(id, index)));
    }
}
