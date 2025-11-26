package vn.hcmute.edu.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.userservice.dto.request.AdminCreateRequest;
import vn.hcmute.edu.userservice.dto.request.AdminUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.AdminResponse;

import java.util.UUID;

public interface AdminService {
    AdminResponse create(AdminCreateRequest dto);

    AdminResponse update(UUID id, AdminUpdateRequest dto);

    void delete(UUID id);

    AdminResponse getById(UUID id);

    Page<AdminResponse> search(String keyword, Pageable pageable);
}