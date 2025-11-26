package vn.hcmute.edu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.userservice.constants.Messages;
import vn.hcmute.edu.userservice.dto.request.SupporterCreateRequest;
import vn.hcmute.edu.userservice.dto.request.SupporterUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.SupporterResponse;
import vn.hcmute.edu.userservice.dto.response.ResponseData;
import vn.hcmute.edu.userservice.service.SupporterService;

import java.util.UUID;

@RestController
@RequestMapping("/supporters")
@RequiredArgsConstructor
public class SupporterController {

    private final SupporterService supporterService;

    @PostMapping
    public ResponseEntity<ResponseData<SupporterResponse>> create(@RequestBody SupporterCreateRequest dto) {
        return ResponseEntity.ok(ResponseData.success(
                Messages.Supporter.CREATED, supporterService.create(dto)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<SupporterResponse>> update(@PathVariable UUID id, @RequestBody SupporterUpdateRequest dto) {
        return ResponseEntity.ok(ResponseData.success(
                Messages.Supporter.UPDATED, supporterService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> delete(@PathVariable UUID id) {
        supporterService.delete(id);
        return ResponseEntity.ok(ResponseData.success(
                Messages.Supporter.DELETED, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<SupporterResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseData.success(supporterService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<SupporterResponse>>> search(
            @RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(ResponseData.success(supporterService.search(keyword, pageable)));
    }
}