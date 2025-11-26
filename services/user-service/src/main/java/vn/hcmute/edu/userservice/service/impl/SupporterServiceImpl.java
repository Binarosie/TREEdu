package vn.hcmute.edu.userservice.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hcmute.edu.userservice.dto.request.SupporterCreateRequest;
import vn.hcmute.edu.userservice.dto.request.SupporterUpdateRequest;
import vn.hcmute.edu.userservice.dto.response.SupporterResponse;
import vn.hcmute.edu.userservice.enums.EUserRole;
import vn.hcmute.edu.userservice.exception.ApiException;
import vn.hcmute.edu.userservice.mapper.SupporterMapper;
import vn.hcmute.edu.userservice.model.SupporterEntity;
import vn.hcmute.edu.userservice.repository.SupporterRepository;
import vn.hcmute.edu.userservice.service.SupporterService;
import vn.hcmute.edu.userservice.specification.SupporterSpecification;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupporterServiceImpl implements SupporterService {

    private final SupporterRepository supporterRepository;
    private final SupporterMapper supporterMapper;
    private final SupporterSpecification supporterSpecification;
    private final Validator validator;

    private <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new ApiException("Validation failed: " + msg, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public SupporterResponse create(SupporterCreateRequest dto) {
        validate(dto);
        if (supporterRepository.existsByEmail(dto.getEmail())) {
            throw new ApiException("Email already exists", HttpStatus.CONFLICT);
        }

        SupporterEntity entity = supporterMapper.toEntity(dto);
        entity.setUserRole(EUserRole.SUPPORTER);

        return supporterMapper.toResponse(supporterRepository.save(entity));
    }

    @Override
    public SupporterResponse update(UUID id, SupporterUpdateRequest dto) {
        validate(dto);

        SupporterEntity existing = supporterRepository.findById(id)
                .orElseThrow(() -> new ApiException("Supporter not found", HttpStatus.NOT_FOUND));

        supporterMapper.updateEntityFromUpdateRequest(dto, existing);

        return supporterMapper.toResponse(supporterRepository.save(existing));
    }

    @Override
    public void delete(UUID id) {
        if (!supporterRepository.existsById(id)) {
            throw new ApiException("Supporter not found", HttpStatus.NOT_FOUND);
        }
        supporterRepository.deleteById(id);
    }

    @Override
    public SupporterResponse getById(UUID id) {
        return supporterRepository.findById(id)
                .map(supporterMapper::toResponse)
                .orElseThrow(() -> new ApiException("Supporter not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public Page<SupporterResponse> search(String keyword, Pageable pageable) {
        Specification<SupporterEntity> spec = supporterSpecification.filter(keyword);
        return supporterRepository.findAll(spec, pageable).map(supporterMapper::toResponse);
    }

}