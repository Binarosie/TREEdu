package vn.hcmute.edu.userservice.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.hcmute.edu.userservice.constants.ValidationMessages;
import vn.hcmute.edu.userservice.dto.request.MemberCreateRequest;
import vn.hcmute.edu.userservice.dto.request.MemberUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.MemberResponse;
import vn.hcmute.edu.userservice.dto.kafka.UserRegistrationEvent;
import vn.hcmute.edu.userservice.enums.EUserRole;
import vn.hcmute.edu.userservice.enums.EUserStatus;
import vn.hcmute.edu.userservice.exception.ApiException;
import vn.hcmute.edu.userservice.mapper.MemberMapper;
import vn.hcmute.edu.userservice.model.MemberEntity;
import vn.hcmute.edu.userservice.repository.MemberRepository;
import vn.hcmute.edu.userservice.service.MemberService;
import vn.hcmute.edu.userservice.specification.MemberSpecification;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final MemberSpecification memberSpecification;

    private <T> void validateDto(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            List<String> errorMessages = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.toList());
            throw ApiException.validationError(errorMessages);
        }
    }

    private void validateBusinessRules(MemberCreateRequest dto) {
        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw ApiException.conflict(ValidationMessages.MEMBER_EMAIL_DUPLICATE);
        }
        if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw ApiException.conflict(ValidationMessages.MEMBER_PHONENUMBER_DUPLICATE);
        }
    }

    private void validateBusinessRulesForUpdate(MemberEntity current, MemberUpdateRequest dto) {
        if (StringUtils.hasText(dto.getEmail()) && !dto.getEmail().equalsIgnoreCase(current.getEmail())) {
            if (memberRepository.existsByEmail(dto.getEmail())) {
                throw ApiException.conflict(ValidationMessages.MEMBER_EMAIL_DUPLICATE);
            }
        }

        if (StringUtils.hasText(dto.getPhoneNumber()) && !dto.getPhoneNumber().equalsIgnoreCase(current.getPhoneNumber())) {
            if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
                throw ApiException.conflict(ValidationMessages.MEMBER_PHONENUMBER_DUPLICATE);
            }
        }
    }

    @Override
    public MemberResponse create(MemberCreateRequest dto) {
        log.info("Creating Member with email: {}", dto.getEmail());

        try {
            validateDto(dto);
            validateBusinessRules(dto);

            log.debug("Validation passed, mapping to entity...");
            MemberEntity entity = memberMapper.toEntity(dto);

            // Hash password manually
            entity.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

            log.debug("Saving entity to database...");
            MemberEntity savedEntity = memberRepository.save(entity);

            log.info("Successfully created Member with ID: {}", savedEntity.getId());
            return memberMapper.toResponse(savedEntity);

        } catch (Exception ex) {
            log.error("Error creating Member with email: {}. Error: {}", dto.getEmail(), ex.getMessage(), ex);
            throw ex; // Re-throw để RestExceptionHandler xử lý
        }
    }

    @Override
    public MemberResponse update(UUID id, MemberUpdateRequest dto) {
        validateDto(dto);

        MemberEntity current = memberRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Member", id.toString()));

        validateBusinessRulesForUpdate(current, dto);

        memberMapper.updateEntityFromUpdateRequest(dto, current);
        return memberMapper.toResponse(memberRepository.save(current));
    }

    @Override
    public void delete(UUID id) {
        MemberEntity entity = memberRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Member", id.toString()));
        memberRepository.delete(entity);
    }

    @Override
    public MemberResponse getById(UUID id) {
        MemberEntity entity = memberRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Member", id.toString()));
        return memberMapper.toResponse(entity);
    }

    @Override
    public Page<MemberResponse> search(String keyword, Pageable pageable) {
        Specification<MemberEntity> spec = memberSpecification.filter(keyword);
        return memberRepository.findAll(spec, pageable)
                .map(memberMapper::toResponse);
    }

    @Override
    @Transactional
    public void createFromKafkaEvent(UserRegistrationEvent.UserData userData) {
        log.info("Creating Member from Kafka event for user: {}", userData.getEmail());

        try {
            // Check if Member already exists by email
            if (memberRepository.existsByEmail(userData.getEmail())) {
                log.warn("Member with email {} already exists, skipping creation", userData.getEmail());
                return;
            }

            // Create Member entity from user data - let JPA handle ID generation
            MemberEntity Member = MemberEntity.builder()
                    .email(userData.getEmail())
                    .fullName(userData.getFullName())
                    .phoneNumber(userData.getPhoneNumber())
                    .passwordHash(userData.getPasswordHash())
                    .userRole(EUserRole.MEMBER) // Set required user role
                    .userStatus(EUserStatus.ACTIVE) // Set required user status
                    .isDeleted(false) // Set deleted flag to false
                    .build();

            MemberEntity savedMember = memberRepository.save(Member);

            log.info("Successfully created Member with ID {} for email: {}",
                    savedMember.getUserId(), userData.getEmail());

        } catch (Exception ex) {
            log.error("Error creating Member from Kafka event for email: {}", userData.getEmail(), ex);
            throw new ApiException("Failed to create Member from user registration event",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}