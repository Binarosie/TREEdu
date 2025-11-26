package vn.hcmute.edu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.hcmute.edu.userservice.constants.Messages;
import vn.hcmute.edu.userservice.dto.request.MemberCreateRequest;
import vn.hcmute.edu.userservice.dto.request.MemberUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.MemberResponse;
import vn.hcmute.edu.userservice.dto.response.ResponseData;
import vn.hcmute.edu.userservice.service.MemberService;

import java.util.UUID;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ResponseData<MemberResponse>> create(@RequestBody MemberCreateRequest dto) {
        MemberResponse response = memberService.create(dto);
        return ResponseEntity.ok(ResponseData.success(
                Messages.Member.CREATED, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<MemberResponse>> update(
            @PathVariable UUID id, @RequestBody MemberUpdateRequest dto) {
        MemberResponse response = memberService.update(id, dto);
        return ResponseEntity.ok(ResponseData.success(
                Messages.Member.UPDATED, response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> delete(@PathVariable UUID id) {
        memberService.delete(id);
        return ResponseEntity.ok(ResponseData.success(
                Messages.Member.DELETED, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<MemberResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseData.success(memberService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseData<Page<MemberResponse>>> search(
            @RequestParam(required = false) String keyword, Pageable pageable) {
        Page<MemberResponse> result = memberService.search(keyword, pageable);
        return ResponseEntity.ok(ResponseData.success(result));
    }

}