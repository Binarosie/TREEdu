package vn.hcmute.edu.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.hcmute.edu.userservice.dto.request.MemberCreateRequest;
import vn.hcmute.edu.userservice.dto.request.MemberUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.MemberResponse;
import vn.hcmute.edu.userservice.dto.kafka.UserRegistrationEvent;

import java.util.UUID;

public interface MemberService {
    MemberResponse create(MemberCreateRequest dto);

    MemberResponse update(UUID id, MemberUpdateRequest dto);

    void delete(UUID id);

    MemberResponse getById(UUID id);

    Page<MemberResponse> search(String keyword, Pageable pageable);

    /**
     * Create Member from Kafka user registration event
     */
    void createFromKafkaEvent(UserRegistrationEvent.UserData userData);
}