package vn.hcmute.edu.userservice.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.userservice.dto.request.SupporterCreateRequest;
import vn.hcmute.edu.userservice.dto.request.SupporterUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.SupporterResponse;

import java.util.Map;
import java.util.UUID;

public interface SupporterService {
    SupporterResponse create(SupporterCreateRequest dto);

    SupporterResponse update(UUID id, SupporterUpdateRequest dto);

    void delete(UUID id);

    SupporterResponse getById(UUID id);

    Page<SupporterResponse> search(String keyword, Pageable pageable);

}